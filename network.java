import java.util.*;
import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;


public class network{
		
		public List <arc> neighbors;
		public List<node> all_nodes;
		public List<client> all_clients;
		public Hashtable <node,List<arc>> network_map;
		public Dictionary<Double,packet> all_packets ;
		public Dictionary<node, List<packet>> node_packet_requests;
		int [] network_bound = new int [2];
		int iteration;

		public network(int [] xy, List <node> nodes, List <client> clients) {
			network_bound =xy;
			all_nodes= nodes;
			all_clients= clients;
			network_map = makeMap();
			iteration=0;
			all_packets = new Hashtable<Double,packet>();
			
			//Queue <packet[]> all_packets ;

		}
		
		
		public Hashtable <node,List<arc>> makeMap() {
	/* Function to make the hashtable of nodes and arcs from the key node to every other node in the network
	 * Intended for use when identifying bad actors 
	 * Through removing the arc from node x to node y if it's weight is greater than all other arcs from node x and adding node y to array of bad actors
	 */
			
			Collections.sort(all_nodes, new SortbyDevice());
			Collections.sort(all_clients, new SortbyDistanceC());
			
			Hashtable <node,List<arc>> network_map = new Hashtable<node, List<arc>>(all_nodes.size()+all_clients.size());


			Iterator<node> node = all_nodes.iterator();
			
			while(node.hasNext()) {
				node Node= node.next();
				Node.connected_devices.addAll(all_nodes);
				Node.connected_devices.addAll(all_clients);
				Node.connected_devices.remove(Node);
				
				ArrayList<arc> Node_Arcs = new ArrayList<arc>();

				for(node Next : Node.connected_devices) {
					arc Node_Node = new arc(Node,Next);
					Node_Arcs.add(Node_Node);
				}
				network_map.put(Node, Node_Arcs);

			}
			
			
			Iterator<client> client = all_clients.iterator();
			
			while(client.hasNext()) {
				client Client = client.next();
				Client.connected_devices.addAll(all_nodes);
				Client.connected_devices.addAll(all_clients);
				Client.connected_devices.remove(Client);
				
				ArrayList<arc> Node_Arcs = new ArrayList<arc>();

				for(node Next : Client.connected_devices) {
					arc Node_Node = new arc(Client,Next);
					Node_Arcs.add(Node_Node);
				}
				network_map.put(Client, Node_Arcs);

			}
			
				
			return network_map;
		}
		
		
		
		public boolean finalStateReached() {
// Check if any nodes have unsent packages. If all nodes have reached the end of their packet processing order, return false
			
			for(node Node : all_nodes) {
				if(Node.hasPackets()) {
					iteration++;
					return false;
				}
			}
			
			
			
			return true;
		}
		
		public void updateNodes() {
// Where all the next packet frame for all nodes are routed and then processed, meaning all packets have been sent to their next nodes.
//Increment the network iterations count every time updateNodes is called.
			for(node Node : all_nodes) {
				
				if(Node.hasPackets()) {
					
					routeNextPacketFrame(Node);
					Node.processPacketFrame();
					Node.startNode();
				}
				
			}
			
//	Unsure if to process by traversing the arcs or by node		
//			for(arc Arc : neighbors) {
//				Arc.updateWeight(iteration);
//				
//				if(Arc.x.hasPackets()) {
//					processNextPacket(Arc.x);
//				}
//				
//			}
			
			iteration++;
		}
		
		
		
		public void routeNextPacketFrame(node Node) {
			
			List<packet> packets = Node.packet_processing_order.get(Node.current_iteration);
			packet P= new packet();
			while(packets!=null && !packets.isEmpty() ) {
				P= packets.remove(0);
				Object[] nextNode= getNextNode(Node,P,Node.current_iteration);
				node next = (node) (nextNode[0]);
				Double Epath_value= (Double) nextNode[1];
				System.out.println("Send Packet size "+P.getSize()+" to "+next.coordinate[0]+","+next.coordinate[1]+" with estimted weight "+Epath_value );
				P.addToPath(next);
				all_packets.put(Epath_value, P);
				//P.addToPath(getBestNextNode(Node, P));
			}

			
		}
		
// I think that Best Next Node can instead take a LinkedList of network path and build onto it using getNextNode
// It can also be a singular recursive function as A* algorithms usually are instead of two overlapping methods
//		
		public static Object[] getNextNode(node initNode, packet P, int iter){
			//System.out.print(".");

			double h = P.getPacketWeight();
			double g = initNode.getNodeCost(P, iter);
			double f = h+g;
		
			if(initNode==P.startNode_endNode[1]) {
				System.out.print("*");

				Object[]node_weight= {initNode, f};
				return node_weight;
			}
			
			node NextNode= initNode;
		
			double min_f = Integer.MAX_VALUE;
			ArrayList<node> nodes = (ArrayList<node>) initNode.connected_devices;
			nodes.removeAll(P.getAllNodes());
			
			node next = new node();
			
			while(!nodes.isEmpty()) {
				next = nodes.remove(0);
				packet p = P;
				p.addToPath(next);
				//System.err.println("^");
				Object[] next_f = getNextNode(next,p, iter+1);

				node Node = (node) next_f[0];
				double F = (double) next_f[1];
				if(F < min_f) {
					NextNode = Node;
					min_f=F;
					
				}
			}

			Object[]node_weight= {NextNode, min_f};
			return node_weight;
			
		}
		
		
//		public static double getNextNodeStep(node initNode, node previous, packet P, int step){
//			
//			if(initNode==P.startNode_endNode[1] | step >= initNode.connected_devices.size()/step ) {
//				return initNode.getNodeCost(P, step);
//			}
//			
//			double h = P.getPacketWeight();
//			double g = previous.getNodeCost(P, initNode.current_iteration);
//			double f = h +g;
//			
//			for(node next : initNode.connected_devices) {
//				double next_f = getNextNode(next,initNode,P,1);
////				if(next_f<min_f) {
////					NextNode = next;
////					min_f=next_f;
////					
////				}
////			}
//			
//			
//			
//			
//			List <arc> Arcs = network_map.get(initNode);
//			Arcs.sort(new sortByWeight(step));
//			
//			
//			node nextNode = Arcs.get(0).y;
//			double current_g = initNode.getNodeCost(P, step);
//			
//			double min_f = Arcs.get(0).edge_weight * current_g;
//			
//			double current_f = min_f*2;
//		
//			for(arc Arc : Arcs) {
//				if(Arc.y != initNode) {
////					node Node = Arcs.get(i).x;
//
//				}
//			}
//			
//			for(int i=1; i<Arcs.size()-1; i++) {
////				current_g = Node.getNodeCost(Node.current_iteration);
//				current_f = Arcs.get(i).edge_weight * (1 + ((10+current_g)/100));
//				
//				if(current_f< min_f) {
////					nextNode = Node;
//					min_f = current_f;
//				}
//				
//			}
//			
//		}
		

//Helper functions for sorting different lists
		
		class sortByWeight implements Comparator<arc> {
			
			int iteration;
			
			public sortByWeight() {
				iteration=0;
			}
			
			public sortByWeight(int i) {
				iteration= i;
			}
			
			public int compare(arc a, arc b)
			{

				return (int) (a.getWeightforState(iteration)  - b.getWeightforState(iteration));
			}
		}
		
		class SortbyDevice implements Comparator<node> {
			public int compare(node a, node b)
			{


				return a.device_number - b.device_number;
			}
		}

		class SortbyDistance implements Comparator<node> {
			public int compare(node a, node b) {
				
				int distance = (a.coordinate[0]- b.coordinate[0])+(a.coordinate[1]- b.coordinate[1]);
				
				return distance;
				
			}
		}
		
		class SortbyDistanceC implements Comparator<client> {
			public int compare(client a, client b) {
				
				int distance = (a.coordinate[0]- b.coordinate[0])+(a.coordinate[1]- b.coordinate[1]);
				
				return distance;
				
			}
		}


	}
