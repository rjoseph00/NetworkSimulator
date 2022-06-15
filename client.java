import java.util.ArrayList;
import java.util.Random;
import java.util.*;


public class client extends node{
		Random rand = new Random();
		//int [] coordinate= new int [2];
		double max_bandwidth = 1.25;
		Integer max_pframe= (int) (max_bandwidth *1538);
		//List < packet> packets_requested;
		//List < packet> packets_received;
		
		public client() {
			
		}

		
		public client(int[] location) {
			coordinate = location;
		}
		
		public List < packet> getPacketsRequested(){
			return packets_requested;
		}
		
		public void requestPackets(List < node> connected_devices) {
//code for clients to request packets from a node by adding the packets to the processing array of the current. 
//Currently hard-coded such that a client can only request from a host server node at the start of the simulation
			int packet_count = rand.nextInt(10);
			for(int i=0; i<=packet_count; i++) {
				node source = connected_devices.get(rand.nextInt(connected_devices.size()-1));
				while(source.device_type!="host") {
					source = connected_devices.get(rand.nextInt(connected_devices.size()-1));
				}
				int bound= (int) (1538 * source.max_bandwidth/1.25);
				
				Integer size = rand.nextInt(1,bound);

				node[] start_end= new node[] { source, (node) this};
				

				packet Packet = new packet(size,start_end);
				packets_requested.add(Packet);
				//source.addPacket(Packet);
			}

		}


	}

