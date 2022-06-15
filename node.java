import java.util.*;

public class node{

		int [] coordinate;
		String device_type;
		int device_number;
		int current_iteration;
		List <client> nearby_clients= new ArrayList<client>();
		List <node> connected_devices= new ArrayList<node>(); 
		List<packet> packets_received= new ArrayList<packet>();
		Queue<packet> packet_queue;
		List<packet> packets_requested= new ArrayList<packet>();
		List<packet> packets_forwarded= new ArrayList<packet>();
		Hashtable< Integer, List<packet> > packet_processing_order = new Hashtable< Integer, List<packet> >(); //Should be renamed frame_processing_order
		Integer max_pframe;
		double max_bandwidth;
		double[] input_bandwidth; //Array of sum size all requested packets at each step
		double[] output_bandwidth; //Array of sum size all forwarded packets at each step

		public node() {
			
		}
		
		public node(Integer max_band) {
			if(max_band==6) {
				device_type= "hub";
				device_number= 1;
			}
			
			if(max_band==5) {
				device_type= "radio";
				device_number= 2;
			}
			
			if(max_band==45) {
				device_type= "bridge";
				device_number= 3;
			}
			
			if(max_band==16) {
				device_type= "host";
				device_number= 4;
			}
			
			max_bandwidth = max_band;
			max_pframe= max_band *1538;
			current_iteration=0;
			
			

		}
		
		public void setLocation(int[] location) {
			coordinate=location;	
		}
		
		public int [] getLocation() {
			return coordinate;
		}
		
		public void setMaxFrame(Integer newSize) {
			max_pframe = newSize;
		}
		
		public void setBandwidth(Integer newBandwidth) {
			max_bandwidth= newBandwidth;
		}

		public void startNode() {
// If the node has received packets for the first time, load the packet_processing_order according to how many packets can be sent in a frame
			int index=0;
			Integer size=0;
			List<packet> current_packets=new ArrayList<packet>();
			
			for(packet P : packets_received) {
				
				// Fragment packet if there is still space in the frame such that a full frame is sent everytime. This helps to implement small message buffer 
				if(P.packet_size>max_pframe - size) {
					packet newPacket = P;
					newPacket.setSize(P.packet_size - (max_pframe-size));
					P.setSize(max_pframe-size);
					int next = packets_received.indexOf(P)+1;
					packets_received.add(next, newPacket);
				}
				
				current_packets.add(P);
				size+= P.packet_size;
				
				if(size>=max_pframe ) {
					
					packet_processing_order.put(index, current_packets);
	
					this.fillPFrames(index, current_packets);
					current_packets.clear();
					size=0;
					index++;
				}
				
			}
			
			this.input_bandwidth = new double[index];
			this.output_bandwidth = new double[index];

			
			for(int i=0; i< packet_processing_order.size(); i++) {
				int output=0;
				int input= 0;
				for(packet P : packet_processing_order.get(i)) {
					
					if(packets_requested.contains(P)) {
						input+=P.packet_size;
					}else {
						output+= P.packet_size;
					}
					//Use Shanon-Hartley for calculating data rate
						input_bandwidth[i]= max_bandwidth / log2(1+(input/output));
						output_bandwidth[i]= max_bandwidth / log2(1+(output/input));
				}
				
			}
			current_iteration++;


		}
		
		public void processPacketFrame() {
			
			for(packet P : packet_processing_order.get(current_iteration)) {
				if(P.startNode_endNode[1].coordinate != coordinate) {
					sendPacket(P, P.current_node);
				}
			}

				current_iteration++;
				
			
		}
		
		public int getPacketIndex(packet P) {
			
//			int index = 0;
			
			for(int i=0; i< packet_processing_order.size(); i++) {
				if(packet_processing_order.get(i).contains(P)) {
					return i;
				}
			}
			
			return packet_processing_order.size();
		}
		
		
		public void addPacket(packet Packet) {
			
			packets_received.add(Packet);
			
			List<packet> nextProcessFrames = new ArrayList<packet>();

			for(int i=current_iteration; i< packet_processing_order.size(); i++) {
				nextProcessFrames.addAll(packet_processing_order.get(i));
			}
			
			nextProcessFrames.add(Packet);
			
			nextProcessFrames.sort(new SortbyArrival());
			
			
			int index = current_iteration;
			int size=0;
			
			List<packet> packetFrame = new ArrayList<packet>();
			
			for(packet P : nextProcessFrames) {
				if(P.packet_size>max_pframe - size) {
					packet newPacket = P;
					newPacket.setSize(P.packet_size - (max_pframe-size));
					P.setSize(max_pframe-size);
					int next = nextProcessFrames.indexOf(P)+1;
					nextProcessFrames.add(next, newPacket);
				}
				
				packetFrame.add(P);
				size+= P.packet_size;
				
				if(size>=max_pframe ) {
					
					packet_processing_order.put(index, packetFrame);
	
					this.fillPFrames(index, packetFrame);
					packetFrame.clear();
					size=0;
					index++;
				}
			}

		}
		
		
		public void sendPacket(packet P, node destination) {
			
			packets_forwarded.add(P);
			destination.addPacket(P);
			P.addToPath(destination);
			//P.updatePacketWeight();			

		}
		
		
		public boolean hasPackets() {
			
			if(packet_processing_order.isEmpty())
				return false;
			
			return true;
		}
		
		public boolean hasPackets(int i) {
			
			if(packet_processing_order.size()<= current_iteration+i)
				return false;
			
			return true;
		}
		


		public void fillPFrames(int process_index, List<packet> packetFrame) {
//update the bandwidths for each packetFrame in the packet_processing_order
			Integer input_size=0;
			Integer output_size=0;
			//input.
			for(packet Packet : packetFrame) {
				if(!packets_forwarded.contains(Packet)) {
					input_size+=Packet.packet_size;
				}else if(packets_forwarded.contains(Packet)){
					output_size+=Packet.packet_size;
				}
			}
			
			input_bandwidth[process_index] = getBandwidth(input_size, output_size);
			output_bandwidth[process_index] = getBandwidth(output_size, input_size);
		}


		
		public static double log2(Integer operand) {
			//Method to calculate log base 2
			return Math.log(operand) / Math.log(2);
		}

		public double getBandwidth(Integer select_packets, Integer other_packets) {
			//estimate bandwidth of node at a given packet frame using the Shannon-Hartley theory
			double estimated_bandwidth = max_bandwidth / log2( (1 + select_packets/other_packets));

			return estimated_bandwidth;
		}

		public double getNodeCost(packet P, int i) {
			
			
			if(i>= packet_processing_order.size()) {
				i = packet_processing_order.size()-1;
			}
			
			List <packet> packets_atSlot= packet_processing_order.get(i);

			
			packets_atSlot.add(P);
			packets_atSlot.sort(new SortbyArrival());
			int packetSlot= packets_atSlot.indexOf(P)+1;
			int next_i = i;
			
			int size=0;
			int offset=0;
			int fragment_size = 0;
			int lastFragment = 0;
						
			for(packet Packet : packets_atSlot) {
				
				
				if(Packet.packet_size + size > max_pframe) {
					
					packetSlot++;
					
					if( (!packet_processing_order.isEmpty()) && i<packet_processing_order.size()-2) {
						next_i = i+1;
					}
					
					if(Packet==P) {
						fragment_size = Packet.packet_size - (max_pframe-size);
						offset= fragment_size / max_pframe;
						next_i +=  offset ;
						lastFragment = fragment_size % (max_pframe * offset);
						break;
					}
					
				}
				
				size+= P.packet_size;
				
			}
			
			if(next_i>packet_processing_order.size()) {
				next_i = packet_processing_order.size()-1;
			}
			
			double processTime = (P.packet_size - fragment_size) / input_bandwidth[i];
			
			processTime+= (max_pframe / max_bandwidth) * offset;
			
			if(coordinate !=P.startNode_endNode[1].coordinate) {
				processTime += (fragment_size / output_bandwidth[next_i]);
			}
			
			
			
			return processTime * (4 / (packetSlot / Math.exp(packetSlot))); 
		}
		
		
		public List <node> sortByArcWeight(List <arc> all_arcs){
			
			all_arcs.sort(new sortByWeight());
			
			for(arc Arc : all_arcs) {
				//connected_devices.
			}
			
			return connected_devices;
		}
		
		
		
		class SortbyArrival implements Comparator<packet> {

			// Method
			// Sorting in ascending order of roll number
			public int compare(packet a, packet b)
			{

				return (int) (a.currentWeight - b.currentWeight);
			}
			
			
		}
		
		class sortByWeight implements Comparator<arc> {

			// Method
			// Sorting in ascending order of roll number
			public int compare(arc a, arc b)
			{

				return (int) (a.edge_weight - b.edge_weight);
			}
		}

		//Integer [ ] output_bandwidth = max_bandwidth / log2( 1 + packet_processing_order.get(input_bandwidth) / packet_processing_order[ i.packets_requested ].sum_size);
	
		
	}
