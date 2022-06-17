import java.util.LinkedList;
import java.util.*;


public class packet{
		LinkedList <arc> network_path; 
		node[] startNode_endNode;
		node current_node;
		Integer packet_size;
		double currentWeight;
		double totalWeight;

		public packet(Integer size) {
			packet_size = size;
		}
		
		
		public packet(Integer size, node[] start_end) {
			//LinkedList <arc> network_path; 
			startNode_endNode = start_end;
			packet_size = size;
			current_node = startNode_endNode[0];
			currentWeight= EuclidianDistance(start_end[0].coordinate, start_end[1].coordinate);
			network_path= new LinkedList<arc>(); 
			totalWeight=0;
		}
		
		public packet() {
			
		}


		public void setSize(Integer newSize) {
			packet_size = newSize;
		}
		
		public void addToPath(node next_node) {
			
			arc NextStop = new arc(current_node, next_node);
			
			current_node = next_node;

			if(next_node.coordinate != startNode_endNode[1].coordinate) {
				network_path.addLast(NextStop);
				currentWeight = getPacketWeight();
			}else {
				//System.out.println("Destination Reached");
			}
		}
		
		public double getPacketWeight(){
			if(this.network_path.isEmpty()) {
				return currentWeight;
			}
			int weight=0;
			int i=0;
			node Node;
			for(arc Arc: network_path) {
				Node = Arc.x;
				weight+= Node.getNodeCost(this, Node.getPacketIndex(this)) + Arc.getWeightforState(i) ;
				i++;
			}	
// Uncomment if the cost of the last node needs to be added
//			
//			Node = network_path.getLast().y;
//			weight+= Node.getNodeCost(this, Node.getPacketIndex(this));
			
			return weight;
		}
		
		public ArrayList<node> getAllNodes() {
			ArrayList <node> nodes = new ArrayList<node> ();
			node Node= current_node;
			nodes.add(Node);
			if(network_path.isEmpty()) {
				return nodes;
			}
			for(arc Arc: network_path) {
				Node = Arc.y;
				if(!nodes.contains(Node))
					nodes.add(Node);
			}	
			
			return nodes;
		}
		
		public Integer getSize() {
			return packet_size;
		}
		
		public node[] getStartEnd() {
			return startNode_endNode;
		}
		
		public LinkedList <arc> getPath(){
			return network_path;
		}
		
		public node getCurrentNode() {
			return current_node;
		}
		
		
		public double EuclidianDistance(int[]x, int[]y) {
			
			return Math.sqrt(Math.pow(x[0]-y[0], 2.0) + Math.pow(x[1]-y[1], 2.0));
		}
		
		
		


	}