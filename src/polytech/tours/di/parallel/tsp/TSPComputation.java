package polytech.tours.di.parallel.tsp;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.Callable;

import polytech.tours.di.parallel.tsp.Instance;
import polytech.tours.di.parallel.tsp.Solution;

/**
 * Implementation des taches paralleles qui estime le meilleur chemin
 * @author Pierrick Bobet, Remy Bouteloup
 * @version 1.0 17/04/12
 */
public class TSPComputation implements Callable<Solution> {

  // Attributs
  private Instance instance;
  private Properties config;
  
  // Constructeur(s)
  /**
   * Construit un nouveau TSPComputation
   * @param instance
   */
  public TSPComputation(Instance instance, Properties config) {
    this.instance = instance;
    this.config = config;
  }

  // Re-definition de la fonction call
  public Solution call() throws Exception {
	  // G�n�ration d'une solution aleatoire et initialisation d'autres solutions
	  Solution bestSolution = generateRamdom(), tempSolution = null, localSearchSolution = null;
	  
	  int counter = 0;

	  // Recupration du temps de max de traitement
	  long max_cpu=Long.valueOf(config.getProperty("maxcpu"));
	  
	  // Demarrage du compteur
	  long startTime=System.currentTimeMillis();
	  
	  // Recherche de la meilleur solution
	  while((System.currentTimeMillis()-startTime)/1_000<=max_cpu) {
		  counter++;
		
	      tempSolution = generateRamdom();
	      localSearchSolution = localSearch(tempSolution);
	     
	      if(localSearchSolution.getOF() < bestSolution.getOF())
	    	  bestSolution = localSearchSolution;
	  }
	  bestSolution.setCountNbOptimLocal(counter);
	  return bestSolution;
  }

  private Solution localSearch(Solution tempSolution) {
	  boolean bestIsFound = true;
	  Solution bestNeighborhood = null;
    
	  while(bestIsFound) {
		  bestNeighborhood = exploreNeighborhood(tempSolution);
      
		  if(bestNeighborhood.getOF() < tempSolution.getOF())
			  tempSolution = bestNeighborhood;
		  else
			  bestIsFound = false;
	  }
	  return tempSolution;
  }

  // Vérifier si la variable tempSolution est vraiment utile, sinon raccourcir dans le if
  private Solution exploreNeighborhood(Solution tempSolution) {
    Solution bestNeighborhood = tempSolution.clone();
    Solution bestTempSolution = tempSolution.clone();
    
    for(int i = 0 ; i < instance.getN() ; i++)
      for(int j = 0 ; j < instance.getN() ; j++) {
        bestNeighborhood.swap(i, j);
        
        // Calcul de la solution
        bestNeighborhood.setOF(TSPCostCalculator.calcOF(instance, bestNeighborhood));
        
        if(bestNeighborhood.getOF() < tempSolution.getOF())
          bestTempSolution = bestNeighborhood.clone();
      }
    return bestTempSolution;
  }

  private Solution generateRamdom() {
    Solution randomSolution = new Solution();
    
    for (int i = 0; i < instance.getN(); i++)
      randomSolution.add(i);
    
    // Mélange les possibilités 
    Collections.shuffle(randomSolution);
    
    // Permet de définir la fonction objective de la solution (en mettant le temps, ansi que la solution)
    randomSolution.setOF(TSPCostCalculator.calcOF(instance, randomSolution));
    return randomSolution;
  }
}