public class TSPTest {

    public static void main(String[] args) {

        RandomUtils.setSeedFromTime(); // nastavi novo seme ob vsakem zagonu main metode (vsak zagon bo drugačen)

        double min = Integer.MAX_VALUE;
        double avg = 0;
        double std = 0;

        // primer zagona za problem eil101.tsp
        for (int i = 0; i < 30; i++) {
            TSP eilTsp = new TSP("a280.tsp", 1000);
            GA ga = new GA(100, 0.8, 0.1);
            TSP.Tour bestPath = ga.execute(eilTsp);

            // shrani min, avg in std
            if (bestPath.getDistance() < min) {
                min = bestPath.getDistance();
            }
            avg += bestPath.getDistance();
            std += Math.pow(bestPath.getDistance(), 2);

        }
        System.out.println(RandomUtils.getSeed()); // izpiše seme s katerim lahko ponovimo zagon
        System.out.println("Min: " + min);
        System.out.println("Avg: " + avg / 30);
        System.out.println("Std: " + Math.sqrt(std / 30 - Math.pow(avg / 30, 2)));

    }
}