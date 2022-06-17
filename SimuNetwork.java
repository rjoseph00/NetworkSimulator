import java.util.*;

public class SimuNetwork {
	private Hashtable<Integer, network> all_networks = new Hashtable<Integer,network>();
	private List<List<packet>> all_packets = new ArrayList<List<packet>>();
	public network currentNetwork;
	public List<packet> currentPackets = new ArrayList<packet>();
	public Random rand= new Random();
//	public int BadClients;
//	public List<client> bad_clients;

	public SimuNetwork() {
		
		
	
	}
	
	public SimuNetwork(int iteration) {
		if(all_networks.size()> iteration ) {
			//currentNetwork = all_networks.
		}
	}
	
	public void reportNetworkContent() {
		
	}
	
	


	public void generateNetwork(int network_type ,int [] input_values) {

		int [] network_boundary = new int[2];
		List<client> newClients = new ArrayList<client>();
		List<node> newNodes= new ArrayList<node>();
		List<int[]> coordinates = new ArrayList <int []>();
		client new_client;
		node new_node;

		Random rand = new Random();

		switch(network_type) {
		case 0: //linear map requires a rectangular graph
			network_boundary[0]= input_values[0]*3;
			network_boundary[1]= input_values[0];
		case 1: //clustered map requires a randomly sized graph
			network_boundary[0]= input_values[0]*rand.nextInt(1, 3);
			network_boundary[1]= input_values[0]*rand.nextInt(1, 3);
		case 2: //even map requires a square graph
			network_boundary[0]= input_values[0]*3;
			network_boundary[1]= input_values[0]*3;
		case 3://a randomLinear map needs a randomly sized graph
			network_boundary[0]= input_values[0]*rand.nextInt(1, 3);
			network_boundary[1]= input_values[0]*rand.nextInt(1, 3);
		}


		for(int n =0; n <= input_values[0]; n++) {
			
			int [] xy = new int[] {rand.nextInt(1,network_boundary[0]+1), rand.nextInt(1,network_boundary[1]+1)};
			coordinates.add(xy);
			new_client= new client(xy);
			newClients.add(new_client);

		}
		


		for(int n =input_values.length-2; n >=1; n--) {
			int []device_bandwidths= new int[] {1, 6, 5, 16, 45};
			
			
			switch(network_type) {
			case 0: //linear map requires dispersing network nodes across map sections
				coordinates= linear(network_boundary, input_values[n]);
			case 1: //clustered map requires placing network nodes at critical regions for max availability
				coordinates = cluster(coordinates,input_values[n]);
			case 2: //even map randomly places network nodes across the map
				coordinates.clear();
				for(int x =0; x <= input_values[n]; x++) {
					coordinates.add(new int[] {rand.nextInt(1, network_boundary[0]+1), rand.nextInt(1,network_boundary[1]+1)});
				}
			case 3:
				coordinates= randomLinear(network_boundary, input_values[n]);
			}
			
			for(int i =0; i< input_values[n]-1; i++) {
				new_node= new node(device_bandwidths[n]);
				new_node.setLocation(coordinates.get(i));
				newNodes.add(new_node);
			}
		}
		
		
		currentNetwork = new network(network_boundary, newNodes, newClients);
		all_networks.put(network_type, currentNetwork);
		
	}
	
	public void RunSimulation() {
		/* When run simulation is called, the routing tables of the network is reported */
		
		//Begin by having each client request a random amount of packets from each node

		ArrayList < ArrayList<packet>> client_requests = new ArrayList < ArrayList<packet>>();
		
		//= new PriorityQueue<packet>(new SortbyArrival());
		for(client Client:currentNetwork.all_clients) {
			Client.requestPackets(currentNetwork.all_nodes);
			client_requests.add(Client.getPacketsRequested());	
		}
		
		System.out.println(client_requests.size()+ " Packets Requested");
		
		
//Using a list of packet arrays , add the packets to their nodes such that each node starts at the first packet request it receives
		
		List<packet> current_requests = new ArrayList<packet>();
		List<node> start_nodes = new ArrayList<node>();
		Dictionary <node, List<packet>> node_requests = new Hashtable<node, List<packet>>();
				
		while(client_requests.size()!=0) {

			ArrayList<packet> requests = client_requests.remove(0);
			packet P= requests.remove(0);
			//P.current_node.addPacket(P);
			current_requests.add(P);
			
			System.out.println("Client at "+P.startNode_endNode[1].coordinate[0]+","+P.startNode_endNode[1].coordinate[1]+" requested packet size "+P.packet_size+" from "+P.current_node.device_type +" at "+P.current_node.coordinate[0]+","+P.current_node.coordinate[1]);
			
			if(!start_nodes.contains(P.current_node)) {
// If the node hasn't been seen yet, add it to the list of running nodes
				List <packet> node_packets = new ArrayList<packet>();
				node_requests.put(P.current_node,node_packets);
				start_nodes.add(P.current_node);
			}
			
			if(node_requests.get(P.current_node)!=null) {
				node_requests.get(P.current_node).add(P);
			}
			
			
			if(!requests.isEmpty()) {
				client_requests.add(requests);
			}else {
			}
			

	}
		
		all_packets.add(current_requests);
		
		currentPackets=current_requests;
		
		for(node Node: start_nodes) {
			while(node_requests.get(Node)!=null && !node_requests.get(Node).isEmpty()) {
				packet P = node_requests.get(Node).remove(0);
				Node.addPacket(P);
			}
			
			
			Node.startNode();
		}

		int iterations = 0;
		
		while(currentNetwork.finalStateReached()==false) {
			
			if(iterations>100) {
				System.out.println("Final State not reached");
				break;
			}
			
			currentNetwork.updateNodes();
			iterations++;
			

			
		}
		

		
		

	}
	

	
	
	
	public List<int[]> linear(int[] boundary, int num){
		List<int []> locations = new ArrayList<int []>(num);
		
		int x= rand.nextInt(1,boundary[0]+1);
		int y= boundary[1] - rand.nextInt(1,boundary[1]+1);

		locations.add(new int[] {x, y});
		
		for(int i=1; i<=num; i++) {

			x= rand.nextInt(1,boundary[0]+1);

			
			locations.add(new int[] {x, y});

		}
		
		return locations;
		
	}
	

	

	
	
	public List<int[]> cluster(List<int[]> coordinates, int clusters){
		
		coordinates.sort(new SortbyDistance());
		
		List<int[]> clusterPoints= coordinates;

		
		List<int[]> newCentroids= new ArrayList<int []>();

		while(clusterPoints.size()>clusters) {
			clusterPoints.sort(new SortbyDistance());
			
			for(int i=clusterPoints.size()-1; i>=1; i--) {
				int[] centroid = new int[2];
				centroid[0]= (clusterPoints.get(i-1)[0] + clusterPoints.get(i)[0]) /2;
				centroid[1]= (clusterPoints.get(i-1)[1] + clusterPoints.get(i)[1]) /2;
				newCentroids.add(centroid);
			}
			if(newCentroids.size()<clusters) {
				int i=1;
				while(newCentroids.size()<= clusters) {
					int[] centroid = new int[2];
					centroid[0]= (clusterPoints.get(i-1)[0] + clusterPoints.get(clusterPoints.size()-i)[0]) /2;
					centroid[1]= (clusterPoints.get(i-1)[1] + clusterPoints.get(clusterPoints.size()-i)[1]) /2;
					newCentroids.add(centroid);
					i++;
				}
			}
			
			
			clusterPoints.clear();
			clusterPoints.addAll(newCentroids);
			newCentroids.clear();

		}
		
		return clusterPoints;
	}
	
	public List<int[]> randomLinear(int[] boundary, int num){
		List<int []> locations = new ArrayList<int []>(num);
		
		int x= rand.nextInt(1,boundary[0]+1);
		int y= boundary[1] - rand.nextInt(1,boundary[1]+1);

		locations.add(new int[] {x, y});
		
		for(int i=1; i<=num; i++) {
			
			switch(rand.nextInt(0,1)) {
			case 0:
				x= rand.nextInt(1,boundary[0]+1);
			case 1:
				y= boundary[1] - rand.nextInt(1,boundary[1]+1);

			}
			x= rand.nextInt(1,boundary[0]+1);
			y= boundary[1] - rand.nextInt(1,boundary[1]+1);

			
			locations.add(new int[] {x, y});

		}
		
		return locations;
		
	}
	
	
	
	
	public static double log2(Integer operand) {
		//Method to calculate log base 2
		return Math.log(operand) / Math.log(2);
	}
	
	
	
	public double EuclidianDistance(int[]x, int[]y) {
		
		return Math.sqrt(Math.pow(x[0]-y[0], 2.0) + Math.pow(x[1]-y[1], 2.0));
	}



	class SortbyArrival implements Comparator<packet> {
		
		// Sorting in ascending order of weight
		public int compare(packet a, packet b){

			return (int) (a.getPacketWeight() - b.getPacketWeight());
		}
	}

	class SortbyDevice implements Comparator<node> {
		public int compare(node a, node b){
			
			return a.device_number - b.device_number;
		}
	}

	
	class SortbyDistance implements Comparator<int[]> {
		public int compare(int[] a, int[] b) {
			
			return (a[0]-b[0])+(a[1]-b[1]);
		}
	}
	
	
	
	



}


