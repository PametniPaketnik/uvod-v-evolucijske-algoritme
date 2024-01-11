import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GA {

    int popSize;
    double cr; //crossover probability
    double pm; //mutation probability

    ArrayList<TSP.Tour> population;
    ArrayList<TSP.Tour> offspring;

    public GA(int popSize, double cr, double pm) {
        this.popSize = popSize;
        this.cr = cr;
        this.pm = pm;
    }

    public TSP.Tour execute(TSP problem) {
        population = new ArrayList<>();
        offspring = new ArrayList<>();
        TSP.Tour best = null;

        for (int i = 0; i < popSize; i++) {
            TSP.Tour newTour = problem.generateTour();
            problem.evaluate(newTour);
            population.add(newTour);
            //TODO shrani najboljšega (best)

            if (best == null || newTour.getDistance() < best.getDistance()) {
                best = newTour.clone();
            }
        }

        while (problem.getNumberOfEvaluations() < problem.getMaxEvaluations()) {

            //elitizem - poišči najboljšega in ga dodaj v offspring in obvezno uporabi clone()
            while (offspring.size() < popSize) {
                TSP.Tour parent1 = tournamentSelection();
                TSP.Tour parent2 = tournamentSelection();
                //TODO preveri, da starša nista enaka

                do {
                    parent2 = tournamentSelection();
                } while (parent1 == parent2);


                if (RandomUtils.nextDouble() < cr) {
                    TSP.Tour[] children = pmx(parent1, parent2);
                    offspring.add(children[0]);
                    if (offspring.size() < popSize)
                        offspring.add(children[1]);
                } else {
                    offspring.add(parent1.clone());
                    if (offspring.size() < popSize)
                        offspring.add(parent2.clone());
                }
            }

            for (TSP.Tour off : offspring) {
                if (RandomUtils.nextDouble() < pm) {
                    swapMutation(off);
                }
            }

            //TODO ovrednoti populacijo in shrani najboljšega (best)
            //implementacijo lahko naredimo bolj učinkovito tako, da overdnotimo samo tiste, ki so se spremenili (mutirani in križani potomci)

            for (TSP.Tour off : offspring) {
                problem.evaluate(off);
                if (off.getDistance() < best.getDistance()) {
                    best = off.clone();
                }
            }

            population = new ArrayList<>(offspring);
            offspring.clear();
        }
        return best;
    }

    private void swapMutation(TSP.Tour off) {
        //izvedi mutacijo

        int tourSize = off.getPath().length;

        int index1 = RandomUtils.nextInt(tourSize);
        int index2;

        do {
            index2 = RandomUtils.nextInt(tourSize);
        } while (index1 == index2);

        // Swap the cities at the chosen indices
        TSP.City city1 = off.getPath()[index1];
        TSP.City city2 = off.getPath()[index2];

        off.setCity(index1, city2);
        off.setCity(index2, city1);
    }

    private TSP.Tour[] pmx(TSP.Tour parent1, TSP.Tour parent2) {
        //izvedi pmx križanje, da ustvariš dva potomca

        int tourSize = parent1.getPath().length;

        int crossoverPoint1  = RandomUtils.nextInt(tourSize);
        int crossoverPoint2;

        do {
            crossoverPoint2 = RandomUtils.nextInt(tourSize);
        } while (crossoverPoint1 == crossoverPoint2);

        if (crossoverPoint1 > crossoverPoint2) {
            int temp = crossoverPoint1;
            crossoverPoint1 = crossoverPoint2;
            crossoverPoint2 = temp;
        }

        return null;
    }

    private TSP.Tour tournamentSelection() {
        // naključno izberi dva RAZLIČNA posameznika in vrni boljšega

        int tournamentSize = 2;
        List<TSP.Tour> tournamentParticipants = new ArrayList<>();

        for (int i = 0; i < tournamentSize; i++) {
            int randomIndex = RandomUtils.nextInt(population.size());
            TSP.Tour participant = population.get(randomIndex);
            tournamentParticipants.add(participant);
        }

        //tournamentParticipants.forEach(participant -> System.out.println(participant.getDistance()));

        return Collections.min(tournamentParticipants, Comparator.comparingDouble(TSP.Tour::getDistance));

        //return null;
    }
}
