package hw1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;

public class homework{

	public static void main(String[] args) { 
		// TODO Auto-generated method stub
		try{
			FileReader inputFile = new FileReader("/Users/zhuge/Desktop/input.txt");
			BufferedReader br = new BufferedReader(inputFile);
			String line = "";
			line = br.readLine();
			String algo = line.trim();
			line = br.readLine();
			String s1[] = line.split(" ");   
	        int w = Integer.parseInt(s1[0]); 
	        int h = Integer.parseInt(s1[1]); 
			int[][] input = new int[w][h];
			line = br.readLine();
			String s2[] = line.split(" ");
			int beginyear = Integer.parseInt(s2[0]); 
	        int beginw = Integer.parseInt(s2[1]); 
	        int beginh = Integer.parseInt(s2[2]);
	        line = br.readLine();
	        String s3[] = line.split(" ");
			int endyear = Integer.parseInt(s3[0]); 
	        int endw = Integer.parseInt(s3[1]); 
	        int endh = Integer.parseInt(s3[2]); 
	        line = br.readLine();
	        int NumberofC = Integer.parseInt(line.trim());
	        HashMap<tunnelValue, LinkedList<tunnelValue>> map = new HashMap<tunnelValue, LinkedList<tunnelValue>>();
	        while ((line = br.readLine()) != null) {
	        	String s4[] = line.split(" ");
	        	Tunnel tunnel = new Tunnel(Integer.parseInt(s4[0]), Integer.parseInt(s4[1]), 
	        			Integer.parseInt(s4[2]), Integer.parseInt(s4[3]));
	        	if(!map.containsKey(tunnel.getOne())) {
	    			//不存在
	    			LinkedList<tunnelValue> list = new LinkedList<>();
	    			list.add(tunnel.getTwo());
	    			map.put(tunnel.getOne(), list);
	    		}else{
	    			//已经存在
	    			LinkedList<tunnelValue> list = map.get(tunnel.getOne());
	    			list.add(tunnel.getTwo());
	    			map.put(tunnel.getOne(), list);
	    		}
	        	if(!map.containsKey(tunnel.getTwo())) {
	    			//不存在
	    			LinkedList<tunnelValue> list = new LinkedList<>();
	    			list.add(tunnel.getOne());
	    			map.put(tunnel.getTwo(), list);
	    		}else{
	    			//已经存在
	    			LinkedList<tunnelValue> list = map.get(tunnel.getTwo());
	    			list.add(tunnel.getOne());
	    			map.put(tunnel.getTwo(), list);
	    		}
		    }
	        br.close();
	     
	        Node res;
	        if(algo.equals("BFS")) {
                res = BFS(input,beginw,beginh,beginyear,endw,endh,endyear,map);
                WriteOutput(res);
            }else if(algo.equals("UCS")) {
               res = UCS(input,beginw,beginh,beginyear,endw,endh,endyear,map);
               WriteOutput(res);
            }else if(algo.equals("A*")){
               res = Astar(input,beginw,beginh,beginyear,endw,endh,endyear,map);
               WriteOutput(res);
             }
	   
	}
		catch (IOException e) {
			e.printStackTrace();
		}
	
	}

	private static Node Astar(int[][] input, int beginw, int beginh, int beginyear, int endw, int endh, int endyear,
			HashMap<tunnelValue, LinkedList<tunnelValue>> map) {
		int next1[][] = {{1,0},{-1,0},{0,1},{0,-1}};//10
		int next2[][] = {{1,1},{-1,-1},{1,-1},{-1,1}};;//14
		Comparator<Node> cmp = new Comparator<Node>(){
			public int compare(Node n1, Node n2){
				return n1.fcost - n2.fcost;
			}
		};
		PriorityQueue<Node> openq = new PriorityQueue<Node>(cmp);
		Queue<Node> children = new LinkedList<>();
		HashMap<Node,Node> visited = new HashMap<Node,Node>();
		HashMap<Node,Node> closeq = new HashMap<Node,Node>();
		Node start = new Node(beginw, beginh, null, beginyear,0,0,14*Math.min(Math.abs(beginw - endw), 
				Math.abs(beginh - endh)) + 10*(Math.abs(beginw - endw) + Math.abs(beginh - endh) - 
						2*Math.min(Math.abs(beginw - endw), Math.abs(beginh - endh))) + Math.abs(beginyear-endyear) ,14*Math.min(Math.abs(beginw - endw), 
								Math.abs(beginh - endh)) + 10*(Math.abs(beginw - endw) + Math.abs(beginh - endh) - 
										2*Math.min(Math.abs(beginw - endw), Math.abs(beginh - endh))) + Math.abs(beginyear-endyear));
		visited.put(start, start);
		openq.offer(start);
		while (!openq.isEmpty()){
			Node temp = openq.poll();
			if (temp.row == endw && temp.column == endh && temp.year == endyear) {
				return temp;
			}
			tunnelValue tempvalue = new tunnelValue(temp.year, temp.row, temp.column);
			if (map.containsKey(tempvalue)){
				LinkedList<tunnelValue> templist = (LinkedList<tunnelValue>) map.get(tempvalue);
				for(int i=0;i<templist.size();i++){//tunnel
		            tunnelValue tempnextvalue = templist.get(i);
			            int r = tempnextvalue.x;
						int c = tempnextvalue.y;
						int y = tempnextvalue.year;
						int newcost = temp.cost + Math.abs(y - temp.year);
						int ecost = 14*Math.min(Math.abs(r - endw), 
								Math.abs(c - endh)) + 10*(Math.abs(r - endw) + Math.abs(c - endh) - 
										2*Math.min(Math.abs(r - endw), Math.abs(c - endh))) + Math.abs(y - endyear);
						int fcost = newcost + ecost;
			            children.offer(new Node(r,c,temp,y,newcost,Math.abs(y - temp.year),ecost,fcost));
		        }
			}
			for (int i = 0; i < 4; i++) {//cost = 10
				int r = temp.row + next1[i][0];
				int c = temp.column + next1[i][1];
				int y = temp.year;
				if (r > input.length - 1 || c > input[1].length - 1|| r < 0 || c < 0) {
					continue;
				}
				int newcost = temp.cost + 10;
				int ecost = 14*Math.min(Math.abs(r - endw), 
						Math.abs(c - endh)) + 10*(Math.abs(r - endw) + Math.abs(c - endh) - 
								2*Math.min(Math.abs(r - endw), Math.abs(c - endh))) + Math.abs(y - endyear);
				int fcost = newcost + ecost;
				children.offer(new Node(r,c,temp,y,newcost,10,ecost,fcost));
			}
			for (int i = 0; i < 4; i++) {//cost = 14
				int r = temp.row + next2[i][0];
				int c = temp.column + next2[i][1];
				int y = temp.year;
				if (r > input.length - 1 || c > input[1].length - 1|| r < 0 || c < 0) {
					continue;
				}
				int newcost = temp.cost + 14;
				int ecost = 14*Math.min(Math.abs(r - endw), 
						Math.abs(c - endh)) + 10*(Math.abs(r - endw) + Math.abs(c - endh) - 
								2*Math.min(Math.abs(r - endw), Math.abs(c - endh))) + Math.abs(y - endyear);
				int fcost = newcost + ecost;
			    children.offer(new Node(r,c,temp,y,newcost,14,ecost,fcost));
			}
			
			while(!children.isEmpty()){
				Node child = children.poll();
				if(visited.containsKey(child)){
					Node existnode = visited.get(child);
					if(closeq.containsKey(existnode)){
						if(existnode.fcost > child.fcost){
							closeq.remove(existnode);
							openq.offer(child);
							visited.put(child, child);
						}
					}else{
						if(existnode.fcost > child.fcost){
							openq.remove(existnode);
							openq.offer(child);
							visited.put(child, child);
						}
					}
				}else{
					openq.offer(child);
					visited.put(child, child);
				}
			}
			closeq.put(temp,temp);
		}
		return null;
	}

	private static Node UCS(int[][] input, int beginw, int beginh, int beginyear,
			int endw,int endh, int endyear, HashMap map){
		int next1[][] = {{1,0},{-1,0},{0,1},{0,-1}};//10
		int next2[][] = {{1,1},{-1,-1},{1,-1},{-1,1}};;//14
		Comparator<Node> cmp = new Comparator<Node>(){
			public int compare(Node n1, Node n2){
				return n1.cost - n2.cost;//choose less cost
			}
		};
		PriorityQueue<Node> openq = new PriorityQueue<Node>(cmp);
		Queue<Node> children = new LinkedList<>();
		HashMap<Node,Node> visited = new HashMap<Node,Node>();
		HashMap<Node,Node> closeq = new HashMap<Node,Node>();
		Node start = new Node(beginw, beginh, null, beginyear,0,0);
		visited.put(start, start);
		openq.offer(start);
		while (!openq.isEmpty()){
			Node temp = openq.poll();
			if (temp.row == endw && temp.column == endh && temp.year == endyear) {
				return temp;
			}
			tunnelValue tempvalue = new tunnelValue(temp.year, temp.row, temp.column);
			if (map.containsKey(tempvalue)){
				LinkedList<tunnelValue> templist = (LinkedList<tunnelValue>) map.get(tempvalue);
				for(int i=0;i<templist.size();i++){
		            tunnelValue tempnextvalue = templist.get(i);
			            int r = tempnextvalue.x;
						int c = tempnextvalue.y;
						int y = tempnextvalue.year;
						int newcost = temp.cost + Math.abs(y - temp.year);
			            children.offer(new Node(r,c,temp,y,newcost,Math.abs(y - temp.year)));
		        }
			}
			for (int i = 0; i < 4; i++) {//cost = 10
				int r = temp.row + next1[i][0];
				int c = temp.column + next1[i][1];
				int y = temp.year;
				if (r > input.length - 1 || c > input[1].length - 1|| r < 0 || c < 0) {
					continue;
				}
				int newcost = temp.cost + 10;
				children.offer(new Node(r,c,temp,y,newcost,10));
			}
			for (int i = 0; i < 4; i++) {//cost = 14
				int r = temp.row + next2[i][0];
				int c = temp.column + next2[i][1];
				int y = temp.year;
				if (r > input.length - 1 || c > input[1].length - 1|| r < 0 || c < 0) {
					continue;
				}
				int newcost = temp.cost + 14;
			    children.offer(new Node(r,c,temp,y,newcost,14));
			}
			
			while(!children.isEmpty()){
				Node child = children.poll();
				if(visited.containsKey(child)){
					Node existnode = visited.get(child);
					if(closeq.containsKey(existnode)){
						if(existnode.cost > child.cost){
							closeq.remove(existnode);
							openq.offer(child);
							visited.put(child, child);
						}
					}else{
						if(existnode.cost > child.cost){
							openq.remove(existnode);
							openq.offer(child);
							visited.put(child, child);
						}
					}
				}else{
					openq.offer(child);
					visited.put(child, child);
				}
			}
			closeq.put(temp,temp);
		}
		return null;
	}

	private static Node BFS(int[][] input, int beginw, int beginh, int beginyear,
			int endw,int endh, int endyear, HashMap map) {
		int next[][] = { { 0, 1 }, { 1, 0 }, { -1, 0 }, { 0, -1 },{ 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 } };
		HashMap<tunnelValue, Integer> visited = new HashMap<>();
		Queue<Node> q = new LinkedList<>();
		Node start = new Node(beginw, beginh, null, beginyear, 0, 0);
		tunnelValue startvalue = new tunnelValue(beginyear,beginw, beginh);
		visited.put(startvalue, 1);//1 means visited
		q.offer(start);
		while (!q.isEmpty()){
			Node temp = q.poll();
			if (temp.row == endw && temp.column == endh && temp.year == endyear) {
				return temp;
			}
			tunnelValue tempvalue = new tunnelValue(temp.year, temp.row, temp.column);
			if (map.containsKey(tempvalue)){//判断时间通道
				LinkedList<tunnelValue> templist = (LinkedList<tunnelValue>) map.get(tempvalue);
				for(int i=0;i<templist.size();i++){
		            tunnelValue tempnextvalue = templist.get(i);
		            if(!visited.containsKey(tempnextvalue)){
		            	visited.put(tempnextvalue, 1);
			            int r = tempnextvalue.x;
						int c = tempnextvalue.y;
						int y = tempnextvalue.year;
						int newcost = temp.cost + 1;
			            q.offer(new Node(r,c,temp,y,newcost,1));
		            }
		        }
			}
				
			for (int i = 0; i < 8; i++) {//判断8个方向
				int r = temp.row + next[i][0];
				int c = temp.column + next[i][1];
				int y = temp.year;
				tunnelValue tempnextvalue = new tunnelValue(y,r,c);
				if (r > input.length - 1 || c > input[1].length - 1|| r < 0 || c < 0 || 
						visited.containsKey(tempnextvalue)) {
					continue;
				}
				int newcost = temp.cost + 1;
				visited.put(tempnextvalue, 1);
				q.offer(new Node(r,c,temp,y,newcost,1));
			}
		}
		return null;
	}
	public static void WriteOutput(Node res) {
		try {	
			String destpath = "/Users/zhuge/Desktop/output.txt";
			File file1 = new File(destpath);		
			if (!file1.exists())
			{	
				file1.delete();
			}
			PrintWriter writer = new PrintWriter(new FileOutputStream(destpath));
			Node parentnode = res;
			Stack<Node> stack = new Stack<Node>();
			stack.push(parentnode);
			int count = 1;
			if(res == null){
				writer.print("FAIL");
			}else{
				writer.println(parentnode.cost);//Output first line
				while(parentnode.parent != null){
					parentnode = parentnode.parent;
					stack.push(parentnode);
					count++;
				}
				writer.println(count);//Output second line
				while(!stack.empty()){
					Node stacknode = stack.pop();
		            writer.println(stacknode.year + " " + stacknode.row + " " + stacknode.column + " " + stacknode.actualcost);
		        }
			}
			writer.close();
		}
		   catch (Exception e)
		   {
			   e.printStackTrace();
		   }
	}
}
	class Node {//cost = total,actual = from last step
		int row;
		int column;
		Node parent;
		int year;
		int cost;
		int actualcost;
		int ecost;
		int fcost;
 
		public Node(int row,int column,Node parent,int year,int cost,int actualcost) {//node1
			super();
			this.row = row;
			this.column = column;
			this.parent = parent;
			this.year = year;
			this.cost = cost;
			this.actualcost = actualcost;
		}
		public Node(int row,int column,Node parent,int year,int cost,int actualcost, int ecost,int fcost) {//node2
			super();
			this.row = row;
			this.column = column;
			this.parent = parent;
			this.year = year;
			this.cost = cost;
			this.actualcost = actualcost;
			this.ecost = ecost;
			this.fcost = fcost;
		}
		   public int hashCode() {
			   int prim = 17;
			   int resultCode = prim*(prim+1) + year;
			   resultCode = prim*resultCode + row;
			   resultCode = prim*resultCode + column;
			   return resultCode;
		   }
		   public boolean equals(Object obj) {
			   if (this == obj) {
				   return true;
			   }
			   if (obj == null) {
				   return false;
			   }
			   if(getClass() != obj.getClass()) {
				   return false;
			   }
			
			   Node mynode = (Node) obj;
			
			   if(this.row != mynode.row) {
				   return false;
			   }
			   if(this.column != mynode.column) {
				   return false;
			   }
			   if(this.year != mynode.year) {
				   return false;
			   }
			   return true;
	       }
	}

	class tunnelValue{
		   int year;
		   int x;
		   int y;
		
		   public tunnelValue(int year, int x, int y){
			   this.year = year;
			   this.x = x;
			   this.y = y;
		   }
		   @Override
		   public int hashCode() {
			   int prim = 17;
			   int resultCode = prim*(prim+1) + year;
			   resultCode = prim*resultCode + x;
			   resultCode = prim*resultCode + y;
			   return resultCode;
		   }
		
		   public boolean equals(Object obj) {
			   if (this == obj) {
				   return true;
			   }
			   if (obj == null) {
				   return false;
			   }
			   if(getClass() != obj.getClass()) {
				   return false;
			   }
			
			   tunnelValue mynode = (tunnelValue) obj;
			
			   if(this.x != mynode.x) {
				   return false;
			   }
			   if(this.y != mynode.y) {
				   return false;
			   }
			   if(this.year != mynode.year) {
				   return false;
			   }
			   return true;
	       }
		   
	   }

	   class Tunnel{
		   int year1;
		   int year2;
		   int x;
		   int y;
		
		   public Tunnel(int year1, int x, int y, int year2) {
			   this.year1 = year1;
			   this.year2 = year2;
			   this.x = x;
			   this.y = y;
		   }
		
		   public tunnelValue getOne(){
			   tunnelValue one = new tunnelValue(year1,x,y);
			   return one;
		   }
		
		   public tunnelValue getTwo() {
			   tunnelValue two = new tunnelValue(year2,x,y);
			   return two;
		   }
		   
	   }
		


