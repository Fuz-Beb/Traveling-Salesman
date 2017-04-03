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
    
    long count = Long.valueOf(config.getProperty("maxcpu"));
    
    Solution bestSolution = null;
    Solution tempSolution = new Solution();
   
    for (int i = 0 ; i < count ; i++) {
      
      tempSolution = getBestSolution();
      
      if(tempSolution.getOF() > bestSolution.getOF())
        bestSolution = tempSolution.clone();
    }
    
    // Affichage test à supprimer plus tard
    System.out.println("Le thread : " + Thread.currentThread().getName() + " a trouvé la meilleure solution suivante :\n" + bestSolution + "\n");
    
    
    return bestSolution;
  }

  private Solution getBestSolution() {
    
    Solution findBest = null;
    
    findBest = generateRamdom();
    
    // Il faut comparer l'attribut best de la classe avec findBest
    
    return findBest;
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
