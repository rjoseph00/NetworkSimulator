import java.util.*;



public class arc{
	public node x;
	public node y;
	public double edge_weight;

	public arc(){


	}
	public arc(Object start, Object end) {
		this.x = (node) start;
		this.y= (node) end;
	}
	
	public arc(node start, node end) {
		this.x = start;
		this.y= end;


	}
	
	public arc(client start, node end) {
		this.x = start;
		this.y= end;


	}
	public arc(node start, client end) {
		this.x = start;
		this.y= end;


	}
	public arc(client start, client end) {
		this.x = start;
		this.y= end;


	}
	
	public void setWeight(double weight) {
		this.edge_weight = weight;
	}
	
	public double getWeight() {
//returns the weight of the arc based on the current bandwidths
		double distance = EuclidianDistance(x.coordinate, y.coordinate);
		
		double sender =(x.hasPackets()? x.output_bandwidth[x.current_iteration] : x.max_bandwidth);
		
		double receiver = (y.hasPackets()? y.input_bandwidth[y.current_iteration] : y.max_bandwidth);
		
		double weight = distance * (sender/receiver);
		
		return weight;	
		}
	
	public double getWeightAt(int statex, int statey) {
//returns the weight of the arc based on the desired bandwidths of processing the packets at a specific slot

		double distance = EuclidianDistance(x.coordinate, y.coordinate);
		
		double sender =(x.hasPackets(0-statex)? x.output_bandwidth[statex] : x.max_bandwidth);
		
		double receiver = (y.hasPackets(0-statey)? y.input_bandwidth[statey] : y.max_bandwidth);
		
		double weight = distance * (sender/receiver);
		
		return weight;	
		}
	
	public double getWeightforState(int iteration) {
//returns the weight of the arc based on the node's bandwidths at the specified iteration of the network

		double distance = EuclidianDistance(x.coordinate, y.coordinate);
		
		double sender =(x.hasPackets(iteration)? x.output_bandwidth[iteration+x.current_iteration] : x.max_bandwidth);
		
		double receiver = (y.hasPackets(iteration)? y.input_bandwidth[iteration+y.current_iteration] : y.max_bandwidth);
		
		double weight = distance * (sender/receiver);
		
		return weight;
		
		//setWeight(weight);
		
		
		//edge_weight= x.;
	}
	
	public double EuclidianDistance(int[]x, int[]y) {
		
		return Math.sqrt(Math.pow(x[0]-y[0], 2.0) + Math.pow(x[1]-y[1], 2.0));
	}
	
}