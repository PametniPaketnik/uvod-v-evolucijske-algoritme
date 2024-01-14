public class TSPMain2 {
    public static void main(String[] args) {

        RandomUtils.setSeedFromTime();

        for (int i = 0; i < 1; i++) {
            TSP eilTsp = new TSP("eil101.tsp", 1000);
            GA ga = new GA(100, 0.8, 0.1);
            TSP.Tour bestPath = ga.execute(eilTsp);

            System.out.println(bestPath.getDistance());
        }
        System.out.println(RandomUtils.getSeed());

    }
}
