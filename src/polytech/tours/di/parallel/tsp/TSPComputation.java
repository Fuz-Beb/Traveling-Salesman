package polytech.tours.di.parallel.tsp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Callable;

import polytech.tours.di.parallel.tsp.Instance;
import polytech.tours.di.parallel.tsp.Solution;

/**
 * Implementation des taches paralleles qui estime le meilleur chemin
 * @author bebo
 *
 */
public class TSPComputation implements Callable<Solution> {

  // Attributs
  private Instance instance;
  Properties config;
  
  // Constructeur(s)
  /**
   * Construit un nouveau TSPComputation
   * @param instance 
   */
  public TSPComputation(Instance instance) {
    this.instance = instance;
  }  
  
  // Re-definition de la fonction call
  @Override
  public Solution call() throws Exception {
    
    // s* = bestSolution
    // s = new randomSolution
    
    long startTime=System.currentTimeMillis();
    long max_cpu = Long.valueOf(config.getProperty("maxcpu"));
    
    Solution bestSolution = generateRamdom(), tempSolution = null, localSearchSolution = null;
   
    while((System.currentTimeMillis()-startTime)/1_000<=max_cpu) {
      
      tempSolution = generateRamdom();
      localSearchSolution = localSearch(tempSolution);
      
      if(localSearchSolution.getOF() < bestSolution.getOF())
        bestSolution = localSearchSolution.clone(); // Redemander au prof si erreur
    }
    
    // Affichage test à supprimer plus tard
    System.out.println("Le thread : " + Thread.currentThread().getName() + " a trouvé la meilleure solution suivante :\n" + bestSolution + "\n");
    
    
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

  private Solution exploreNeighborhood(Solution tempSolution) {
    
    Solution bestNeighborhood = null;
    Solution swapSolution = null;
    
    for(int i = 0 ; i < instance.getN() ; i++)
      for(int j = 0 ; j < instance.getN() ; j++) {
        swapSolution.swap(i, j);
        
        if(swapSolution.getOF() < bestNeighborhood.getOF())
          bestNeighborhood = swapSolution;
      }
        
    return bestNeighborhood;
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
