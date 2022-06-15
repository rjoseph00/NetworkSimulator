import java.util.LinkedList;
import java.util.*;


public class packet{
		LinkedList <arc> network_path; 
		node[] startNode_endNode;
		node current_node;
		Integer packet_size;
		double currentWeight;
		double totalWeight;

		public packet(Integer size, node[] start_end) {
			//LinkedList <arc> network_path; 
			startNode_endNode = start_end;
			packet_size = size;
			current_node = startNode_endNode[0];
			currentWeight= EuclidianDistance(start_end[0].coordinate, start_end[1].coordinate);
			totalWeight=0;
		}
		
		public void setSize(Integer newSize) {
			packet_size = newSize;
		}
		
		public void addToPath(node next_node) {
			
			if(next_node.coordinate != current_node.coordinate) {
				arc NextStop = new arc(current_node, next_node);
				network_path.addLast(NextStop);
				current_node = next_node;
			}else {
				System.out.println("Destination Reached");
			}
		}
		
		public double getPacketWeight(){
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
		
		
		public double EuclidianDistance(int[]x, int[]y) {
			
			return Math.sqrt(Math.pow(x[0]-y[0], 2.0) + Math.pow(x[1]-y[1], 2.0));
		}
		
		
		


	}