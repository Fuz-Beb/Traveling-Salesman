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
		
	public Solution run(Properties config)
	{
		// D√©claration de la variable qui contiendra le meilleur r√©sultat de l'ensemble des threads
		Solution theBestSolution = null;

		// Initialisation d'un service d'execution
		ExecutorService executor = Executors.newFixedThreadPool(Integer.valueOf(config.getProperty("threads")));

		// Lecture d'une instance
		InstanceReader ir = new InstanceReader();
		ir.buildInstance(config.getProperty("instance"));

		// R√©cup√©ration de l'instance
		Instance instance=ir.getInstance();

		// ?? CALCUL DU NOMBRE D'ITERATION ?? // A DEMANDER AU PROF

		// D√©claration des structures de stockage
		List<Future<Solution>> results = new ArrayList<>();
		List<Callable<Solution>> tasks = new ArrayList<>();

		// Affection des t√¢ches √† faire dans la structure de stockage
		for (int i = 0 ; i < Long.valueOf(config.getProperty("threads")) ; i++)
			tasks.add(new TSPComputation(instance));

		// Lancement des threads
		try
		{
			System.out.println("Lancement des threads \n");
			results = executor.invokeAll(tasks);
			executor.shutdown();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Comparaison du meilleur rÈsultat de chaque threads
		try
		{
			// RÈcupÈration du premier rÈsultat pour le comparer un ‡ un aux autres.
			theBestSolution = results.get(0).get().clone();
			
			// Comparaison
			for (int j = 1 ; j < results.size() ; j++)
			{
				if (results.get(j).get().getOF() < theBestSolution.getOF())
					theBestSolution = results.get(j).get().clone();
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		
		// Retourne la meilleur des meilleurs solutions
		return theBestSolution;
	}
}
