package polytech.tours.di.parallel.tsp;

import polytech.tours.di.parallel.tsp.Algorithm;
import polytech.tours.di.parallel.tsp.Instance;
import polytech.tours.di.parallel.tsp.InstanceReader;
import polytech.tours.di.parallel.tsp.Solution;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * ParallelAlgorithm.java
 * Purpose : Create and launch threads to search the shortest way. Print the resultat at the end.  
 * 
 * @author Pierrick Bobet, Remy Bouteloup
 * @version 1.0 17/04/14
 */
public class ParallelAlgorithm implements Algorithm {
	
	/**
	 * Store severals time to do experimentations.
	 */
	final static private String[] timeCpu = {"5", "15", "35", "60"};
	
	/**
	 * Store severals quantity of threads to do experimentations.
	 */
	final static private String[] nbThreads = {"1", "5", "20", "50", "100", "200"};
		
	/**
	 * Run threads to do the calculation and print results.
	 * 
	 * @param config The file configuration XML
	 * @return Nothing
	 */
	public void run(Properties config)
	{
		// This solution will store the best
		Solution theBestSolution = null;
		
		// Initalize executor service
		ExecutorService executor;

		// Read one instance (file)
		InstanceReader ir = new InstanceReader();
		ir.buildInstance(config.getProperty("instance"));
		Instance instance=ir.getInstance();

		// These lists store tasks and results future.
		List<Future<Solution>> results = new ArrayList<>();
		List<Callable<Solution>> tasks = new ArrayList<>();
		
		// Counters
		int loopTime = 0, loopThreads = 0, nbLocalOptimum = 0;
		double ofMoy = 0;

		// When all value of timeCpu will be used, the loop will be stopped.
		while (timeCpu.length > loopTime) {
			// Set configuration to the config file
			config.setProperty("maxcpu", timeCpu[loopTime]);
			config.setProperty("threads", nbThreads[loopThreads]);
			
			// Setup threads to the executor.
			executor = Executors.newFixedThreadPool(Integer.valueOf(config.getProperty("threads")));
			
			// Assignments of tasks to the list 
			for (int i = 0 ; i < Integer.valueOf(config.getProperty("threads")) ; i++)
				tasks.add(new TSPComputation(instance, config));
			
			// Run and shutdown threads
			try	{
				results = executor.invokeAll(tasks);
				executor.shutdown();
			} catch (Exception e) {
				e.printStackTrace();
			}

			try
			{
				// Recover the first solution to do comparison
				theBestSolution = results.get(0).get().clone();
				nbLocalOptimum = theBestSolution.getCountNbOptimLocal();
				ofMoy = theBestSolution.getOF();
				
				// Search the shortest way, the totally of optimum local and an average of the best way founded by each threads.
				for (int j = 1 ; j < results.size() ; j++)
				{
					nbLocalOptimum += results.get(j).get().getCountNbOptimLocal();
					ofMoy += results.get(j).get().getOF();
					
					// Search the shortest way
					if (results.get(j).get().getOF() < theBestSolution.getOF())
						theBestSolution = results.get(j).get().clone();
				}
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
			// Round variable to improve printing
			ofMoy = Math.round(ofMoy);
			theBestSolution.setOF(Math.round(theBestSolution.getOF()));
			
			// Print results
			System.out.println("TIME\tTASKS\tTHREADS\tOPTIMUMLOCAL\tOFMIN\tOFMOY");
			System.out.println(Long.valueOf(config.getProperty("maxcpu")) + "\t" + Integer.valueOf(config.getProperty("threads")) + "\t" + Integer.valueOf(config.getProperty("threads")) + "\t" + nbLocalOptimum + "\t\t" + theBestSolution.getOF() + "\t" + ofMoy / Integer.valueOf(config.getProperty("threads")) + "\t\n");
			
			// Delete all data in lists
			results.clear();
			tasks.clear();
			
			// Update counters
			loopThreads++;
			if (loopThreads == nbThreads.length) {
				loopThreads = 0;
				loopTime++;
			}
		}
	}
}