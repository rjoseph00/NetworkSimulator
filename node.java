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
		Hashtable< Integer, List<packet> > packet_processing_order; //Should be renamed frame_processing_order
		Integer max_pframe; //Comparable to Max Transmission Unit
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
			
			max_pframe= max_band *1538;
			max_bandwidth = max_band;

			current_iteration=0;
			packet_processing_order = new Hashtable< Integer, List<packet> >();
			
			

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
			int index= 0;
			Integer size=0;
			List<packet> current_packets=new ArrayList<packet>();
			int in_count=0;
			int out_count=0;
			
			Iterator<packet> packets= packets_received.iterator();
			
			while(packets.hasNext()) {
				packet P = packets.next();
//			}
//			for(packet P : packets_received) {
				
				// Fragment packet if there is still space in the frame such that a full frame is sent everytime. This helps to implement small message buffer 
				if(P.packet_size>max_pframe - size) {
					packet newP = P;
					packet fragP =P;
					fragP.setSize(P.packet_size - (max_pframe-size));
					newP.setSize(max_pframe-size);
					int next = packets_received.indexOf(P);
										
					packets_received.add(next, fragP);

					packets_received.add(next, newP);

					packets_received.remove(P);
					
					packets= packets_received.iterator();



					
				}
				
				current_packets.add(P);
				size+= P.packet_size;
				
				if(size>=max_pframe ) {
					
					packet_processing_order.put(index, current_packets);
	
					//this.fillPFrames(index, current_packets);
					current_packets.clear();
					size=0;
					index++;
				}
				
			}
			
			this.input_bandwidth = new double[index+1];
			this.output_bandwidth = new double[index+1];

			
			for(int i=0; i<= index-1; i++) {
				int output=0;
				int input= 0;
				
				packet P = new packet();
				List<packet> process_packets = packet_processing_order.get(i);
				
				while(!process_packets.isEmpty() && process_packets!=null) {
					P= process_packets.remove(i);
					if(packets_requested.contains(P)) {
						input+=P.packet_size;
						in_count++;
					}else {
						output+= P.packet_size;
						out_count++;
					}

				}
				
				in_count= in_count==0?1:in_count;
				in_count= out_count==0?1:out_count;

				
			//Use Shanon-Hartley for calculating data rate
				input_bandwidth[i]= output==0?max_bandwidth:( max_bandwidth*log2( 1+( (input/in_count)/(output/out_count) ) ) ) ;
				output_bandwidth[i]= input==0?max_bandwidth:(max_bandwidth * log2(1+((output/out_count)/(input/in_count)))) ;
				
				
				
			}
			current_iteration++;


		}
		
		public void processPacketFrame() {
			
			
			//for(packet P : packet_processing_order.get(current_iteration)) {
			while(packet_processing_order.get(current_iteration)!=null && !packet_processing_order.get(current_iteration).isEmpty()) {
				packet P = packet_processing_order.get(current_iteration).remove(0);
			
				if(P.startNode_endNode[1].coordinate != coordinate) {
					sendPacket(P, P.current_node);
				}
				packets_received.remove(P);
			}

				current_iteration--;
				
			
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
			Integer input=0;
			Integer output=0;
			int out_count=0;
			int in_count= 0;
			//input.
			for(packet Packet : packetFrame) {
				if(!packets_forwarded.contains(Packet)) {
					input+=Packet.packet_size;
				}else if(packets_forwarded.contains(Packet)){
					output+=Packet.packet_size;
				}
			}
			
			input_bandwidth[process_index]= output==0?max_bandwidth:( max_bandwidth*log2( 1+( (input/in_count)/(output/out_count) ) ) ) ;
			output_bandwidth[process_index]= input==0?max_bandwidth:(max_bandwidth * log2(1+((output/out_count)/(input/in_count)))) ;
		}


		
		public static double log2(Integer operand) {
			//Method to calculate log base 2
			return Math.log(operand) / Math.log(2);
		}

		public double getBandwidth(Integer select_packets, Integer other_packets) {
			//estimate bandwidth of node at a given packet frame using the Shannon-Hartley theory
			double estimated_bandwidth = max_bandwidth * log2( (1 + select_packets/other_packets));

			return estimated_bandwidth;
		}

		
		public double[] getProcessTime_PacketSlot(ArrayList<packet> packets_atSlot, packet P, int i) {
			
			if(packets_atSlot.isEmpty()) {
				double[] time_slot = { (P.packet_size/ max_bandwidth) , 1};
				return time_slot;			
			}
			
			if(i>= packet_processing_order.size()) {
				i = packet_processing_order.size()-1;
			}
			
			packets_atSlot.add(P);
			packets_atSlot.sort(new SortbyArrival());

			int packetSlot= packets_atSlot.indexOf(P)+1;
			
			int next_i = i;
			
			int size=0;
			double processTime=0;
// Determine if the packet will be fully processed within the current packet frame
			
//			Queue<packet> packets = new LinkedList<packet> (); 
//			packets.addAll(packets_atSlot);
			
			packet Packet = new packet();
			
			while(!packets_atSlot.isEmpty()) {
//			for(packet Packet : packets_atSlot) {
				Packet = packets_atSlot.remove(0);
				if(Packet.packet_size + size > max_pframe) {
					
					if(Packet!=P) {
						next_i = i+1;
					}
					if(Packet==P) {
						int fragment_size = Packet.packet_size - (max_pframe-size);
						int offset= fragment_size<=max_pframe? 1 :fragment_size / max_pframe ;
						next_i +=  offset;
						int lastFragment = fragment_size % (max_pframe * offset);
						
						processTime = (P.packet_size - fragment_size) / input_bandwidth[i];
						processTime+= (max_pframe / max_bandwidth) * offset;

						if(lastFragment!=0) {
							
							if(next_i<=packet_processing_order.size()-1)
								packets_atSlot.addAll(packet_processing_order.get(next_i));
							
							packet lastP = new packet();
							lastP= P;
							lastP.setSize(lastFragment);
							
							double [] nextTime_Slot= getProcessTime_PacketSlot(packets_atSlot, lastP, next_i);
							
							processTime+= nextTime_Slot[0];
							
							packetSlot+= nextTime_Slot[1];
							
							if(coordinate !=P.startNode_endNode[1].coordinate) {
								processTime += (fragment_size / output_bandwidth[next_i]);
							}							
						}												
					}
					
				}else if(Packet==P) {
					processTime = (P.packet_size) / input_bandwidth[i];
					
					if(coordinate !=P.startNode_endNode[1].coordinate) {
						processTime += (P.packet_size / output_bandwidth[next_i]);
					}
				}
				
				
				size+= P.packet_size;
			}
			
				
			
			double[] time_slot = {processTime,packetSlot};
			return time_slot;
			
			
			
		}
		
		
		
		public double getNodeCost(packet P, int i) {
			
			if(packet_processing_order.isEmpty()) {
				return (P.packet_size/ max_bandwidth) * (4 / (1 / Math.exp(1)));			
			}
			
			if(i>= packet_processing_order.size()) {
				i = packet_processing_order.size()-1;
			}
			
			ArrayList <packet> packets_atSlot= (ArrayList<packet>) packet_processing_order.get(i);
			
			
			double[]time_slot= getProcessTime_PacketSlot(packets_atSlot, P, i);
			
			
			
			return time_slot[0] * (4 / (time_slot[1] / Math.exp(time_slot[1]))); 
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

				return (int) (a.getPacketWeight() - b.getPacketWeight());
			}
			
			
		}
		
		class sortByWeight implements Comparator<arc> {

			// Method
			// Sorting in ascending order of roll number
			public int compare(arc a, arc b)
			{

				return (int) ( a.getWeight() - b.getWeight() );
			}
		}

		//Integer [ ] output_bandwidth = max_bandwidth * log2( 1 + packet_processing_order.get(input_bandwidth) / packet_processing_order[ i.packets_requested ].sum_size);
	
		
	}
