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

public class ParallelAlgorithm implements Algorithm {
	
	final static private String[] timeCpu = {"5", "15", "35", "60"};
	final static private String[] nbThreads = {"1", "5", "20", "50", "100", "200"};
		
	public void run(Properties config)
	{
		// D√©claration de la variable qui contiendra le meilleur r√©sultat de l'ensemble des threads
		Solution theBestSolution = null;
		
		// Initialisation d'un service d'execution
		ExecutorService executor;

		// Lecture d'une instance
		InstanceReader ir = new InstanceReader();
		ir.buildInstance(config.getProperty("instance"));

		// R√©cup√©ration de l'instance
		Instance instance=ir.getInstance();

		// D√©claration des structures de stockage
		List<Future<Solution>> results = new ArrayList<>();
		List<Callable<Solution>> tasks = new ArrayList<>();
		
		// Counters
		int loopTime = 0, loopThreads = 0, nbLocalOptimum = 0;
		double ofMoy = 0;

		while (timeCpu.length > loopTime) {
			// Set configuration to the config file
			config.setProperty("maxcpu", timeCpu[loopTime]);
			config.setProperty("threads", nbThreads[loopThreads]);
			
			// Initialisation d'un service d'execution
			executor = Executors.newFixedThreadPool(Integer.valueOf(config.getProperty("threads")));
			
			// Affection des taches a faire dans la structure de stockage 
			for (int i = 0 ; i < Integer.valueOf(config.getProperty("threads")) ; i++)
				tasks.add(new TSPComputation(instance, config));

			// Lancement des threads
			try	{
				results = executor.invokeAll(tasks);
				executor.shutdown();
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Comparaison du meilleur rÈsultat de chaque threads
			try
			{
				// RÈcupÈration du premier rÈsultat pour le comparer un ‡ un aux autres.
				theBestSolution = results.get(0).get().clone();
				nbLocalOptimum = theBestSolution.getCountNbOptimLocal();
				ofMoy = theBestSolution.getOF();
				
				// Comparaison
				for (int j = 1 ; j < results.size() ; j++)
				{
					// RÈcupÈration de la moyenne et du nombre d'optimum local
					nbLocalOptimum += results.get(j).get().getCountNbOptimLocal();
					ofMoy += results.get(j).get().getOF();
					
					if (results.get(j).get().getOF() < theBestSolution.getOF())
						theBestSolution = results.get(j).get().clone();
				}
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
			ofMoy = Math.round(ofMoy);
			theBestSolution.setOF(Math.round(theBestSolution.getOF()));
			
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

























