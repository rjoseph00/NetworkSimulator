# NetworkSimulator
Network Simulator: Final Project for Middlebury College's Artificial Intelligence Course

Project Debrief:

   Goals-
          Create interconnected mesh map of different densities
          Simulate IoT network packet routing using A* heuristic at 3 volume levels
   
   Main Methods- 
          Edge cost, h(n) is estimated by coordinate distance between nodes * start_node.output_bandwidth / end_node.input_bandwidth
          Node cost, g(n), will be based on the Slotted ALOHA transmission efficiency function Ge-G where G is the slot where packet P will be processed.
          Estimated message latency for current Packet P.size at Node n = (P.size/output_bandwidth) * (4 / (Ge-G));


          Current State of Project:
          successful in generating network nodes and no errors returned with pre-set input values.
          Returns null due to unshared memory
          Returns out of bounds errors when Client<=Host<=Hub<=Radio<=Bridge


