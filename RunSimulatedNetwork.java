import java.util.*;


public class RunSimulatedNetwork {
	private static SimuNetwork simulation = new SimuNetwork();

	public RunSimulatedNetwork(String [] args) {
		main(args);
		// TODO Auto-generated constructor stub
	}
	
	public void addSimulation() {
		
	}

	public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
		System.out.println("Enter Network Type: linear(0) clustered(1) random(2);");
		char c = scan.nextLine().charAt(0);
		Integer network_type = c - '0';
		System.out.println("Auto-Generate Nodes? y / n");
		c = scan.nextLine().charAt(0);
		int[] nodes = new int[6];
		if( c =='n') {
			System.out.println("Enter Total Number of Clients: ");
			c = scan.nextLine().charAt(0);
			nodes[0]=c - '0';
			
			System.out.println("Enter Total Number of WiFI Hubs: ");
			c = scan.nextLine().charAt(0);
			nodes[1]=c - '0';
			
			System.out.println("Enter Total Number of Wireless Radio Towers: ");
			c = scan.nextLine().charAt(0);
			nodes[2]=c - '0';

			System.out.println("Enter Total Number of Host Server Routers: ");
			c = scan.nextLine().charAt(0);
			nodes[3]=c - '0';
			
			System.out.println("Enter Total Number of Broadband Bridges: ");
			c = scan.nextLine().charAt(0);
			nodes[4]=c - '0';
			
		}else {
			System.out.println("Pick Network Size sparse(0) average(1) dense(2)");
			c = scan.nextLine().charAt(0);
			int x = c - '0';
			switch(x) {
				case 0:
					nodes = new int[] {25, 15, 12, 6, 3,0};
				case 1:
					nodes = new int[] {50, 30, 24, 12, 6,0};
				case 2:
					nodes = new int[] {100, 60, 48, 24, 12,0};
				
			}
			
		}
		
		System.out.println("Enter Total Number of Bad Actors: ");
		c = scan.nextLine().charAt(0);
		nodes[5] = c - '0';
		
		System.out.println("Generating New Simulated Network...");
		simulation.generateNetwork(network_type,nodes);
		
		simulation.reportNetworkContent();

		
		//System.out.println("Enter Number of Iterations to Run: ");
		//c = scan.nextLine().charAt(0);
		//int iterations = c - '0';
		
		//while (iterations>0) {
		simulation.RunSimulation();
		//}
		
				
		
		
		System.out.println("Max Packets per iteration");
		
		// TODO Auto-generated method stub

	}

}
