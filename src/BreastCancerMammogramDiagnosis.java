import generic.Input;
import generic.Output;
import generic.Tuple;
import tools.JMathPlotter;
import type1.sets.T1MF_Interface;
import type1.sets.T1MF_Trapezoidal;
import type1.sets.T1MF_Triangular;
import type1.system.T1_Antecedent;
import type1.system.T1_Consequent;
import type1.system.T1_Rule;
import type1.system.T1_Rulebase;

public class BreastCancerMammogramDiagnosis {
    Input calcification, mass; // the inputs to the FLS
    Output impression; // the output of the FLS
    T1_Rulebase rulebase; // the rulebase captures the entire FLS

    public BreastCancerMammogramDiagnosis() {
        // Define the inputs
        calcification = new Input("Calcification", new Tuple(0, 10)); // a rating given by a person between 0 and 10
        mass = new Input("Mass", new Tuple(0, 30)); // a rating given by a person between 0 and 10
        impression = new Output("Impression", new Tuple(-20, 20)); // a percentage for the tip

        // Set up the membership functions (MFs) for each input and output
        double[] lessCalcificationParams = { 0.0, 0.0, 2.0, 4.0 };
        double[] moreCalcificationParams = { 6.0, 8.0, 10.0, 10.0 };

        T1MF_Trapezoidal lessCalcificationMF = new T1MF_Trapezoidal("MF for less calcification",
                lessCalcificationParams);
        T1MF_Triangular severalCalcificationMF = new T1MF_Triangular("MF for several calcification", 2.0, 5.0, 8.0);
        T1MF_Trapezoidal moreCalcificationMF = new T1MF_Trapezoidal("MF for more calcification",
                moreCalcificationParams);

        double[] smallMassParams = { 0.0, 0.0, 4.0, 12 };
        double[] largeMassParams = { 17.0, 25.0, 30.0, 30.0 };

        T1MF_Trapezoidal smallMassMF = new T1MF_Trapezoidal("MF for small mass", smallMassParams);
        T1MF_Triangular mediumMassMF = new T1MF_Triangular("MF for medium mass", 5.0, 15.0, 25.0);
        T1MF_Trapezoidal largeMassMF = new T1MF_Trapezoidal("MF for large mass", largeMassParams);

        T1MF_Triangular benignImpressionMF = new T1MF_Triangular("Benign", -20.0, -10.0, 0.0);
        T1MF_Triangular suspiciousMalignantMF = new T1MF_Triangular("Suspicious maglinant", -10.0, 0.0, 10.0);
        T1MF_Triangular malignantMF = new T1MF_Triangular("Malignant", 0.0, 10.0, 20.0);

        // Set up the antecedents and consequents - note how the inputs are
        // associated...
        T1_Antecedent lessCalcification = new T1_Antecedent("lessCalcification", lessCalcificationMF, calcification);
        T1_Antecedent severalCalcification = new T1_Antecedent("SeveralCalcification", severalCalcificationMF, calcification);
        T1_Antecedent moreCalcification = new T1_Antecedent("MoreCalcification", moreCalcificationMF, calcification);

        T1_Antecedent smallMass = new T1_Antecedent("SmallMass", smallMassMF, mass);
        T1_Antecedent mediumMass = new T1_Antecedent("MediumMass", mediumMassMF, mass);
        T1_Antecedent largeMass = new T1_Antecedent("LargeMass", largeMassMF, mass);

        T1_Consequent benign = new T1_Consequent("Benign", benignImpressionMF, impression);
        T1_Consequent suspiciousMalignant = new T1_Consequent("SuspiciousMalignant", suspiciousMalignantMF, impression);
        T1_Consequent malignant = new T1_Consequent("Malignant", malignantMF, impression);

        // Set up the rulebase and add rules
        rulebase = new T1_Rulebase(9);
        rulebase.addRule(new T1_Rule(new T1_Antecedent[] { lessCalcification, smallMass }, benign));
        rulebase.addRule(new T1_Rule(new T1_Antecedent[] { lessCalcification, mediumMass }, benign));
        rulebase.addRule(new T1_Rule(new T1_Antecedent[] { lessCalcification, largeMass }, benign));
        rulebase.addRule(new T1_Rule(new T1_Antecedent[] { severalCalcification, smallMass }, suspiciousMalignant));
        rulebase.addRule(new T1_Rule(new T1_Antecedent[] { severalCalcification, mediumMass }, suspiciousMalignant));
        rulebase.addRule(new T1_Rule(new T1_Antecedent[] { severalCalcification, largeMass }, suspiciousMalignant));
        rulebase.addRule(new T1_Rule(new T1_Antecedent[] { moreCalcification, smallMass }, malignant));
        rulebase.addRule(new T1_Rule(new T1_Antecedent[] { moreCalcification, mediumMass }, malignant));
        rulebase.addRule(new T1_Rule(new T1_Antecedent[] { moreCalcification, largeMass }, malignant));

        // just an example of setting the discretisation level of an output - the usual
        // level is 100
        impression.setDiscretisationLevel(100);

        // get some outputs
        getImpression(7, 15);

        // plot some sets, discretizing each input into 100 steps.
        plotMFs("Calcification Membership Functions",
                new T1MF_Interface[] { lessCalcificationMF, severalCalcificationMF, moreCalcificationMF },
                calcification.getDomain(), 100);
        plotMFs("Mass Membership Functions", new T1MF_Interface[] { smallMassMF, mediumMassMF, largeMassMF },
                mass.getDomain(), 100);
        plotMFs("Impression Membership Functions",
                new T1MF_Interface[] { benignImpressionMF, suspiciousMalignantMF, malignantMF }, impression.getDomain(),
                100);

        // plot control surface
        // do either height defuzzification (false) or centroid d. (true)
        plotControlSurface(true, 100, 100);

        // print out the rules
        System.out.println("\n" + rulebase);
    }

    /**
     * Basic method that prints the output for a given set of inputs.
     *
     * @param calcificationInput
     * @param massInput
     */
    private void getImpression(double calcificationInput, double massInput) {
        // first, set the inputs
        calcification.setInput(calcificationInput);
        mass.setInput(massInput);
        // now execute the FLS and print output
        System.out.println("The calcification was: " + calcification.getInput());
        System.out.println("The mass was: " + mass.getInput());
        System.out.println("Using height defuzzification, the FLS recommends"
                + "an impression of: " + rulebase.evaluate(0).get(impression));
        System.out.println("Using centroid defuzzification, the FLS recommends"
                + "an impression of: " + rulebase.evaluate(1).get(impression));
    }

    private void plotMFs(String name, T1MF_Interface[] sets, Tuple xAxisRange, int discretizationLevel) {
        JMathPlotter plotter = new JMathPlotter(17, 17, 15);
        for (int i = 0; i < sets.length; i++) {
            plotter.plotMF(sets[i].getName(), sets[i], discretizationLevel, xAxisRange, new Tuple(0.0, 1.0), false);
        }
        plotter.show(name);
    }

    private void plotControlSurface(boolean useCentroidDefuzzification, int input1Discs, int input2Discs) {
        double output;
        double[] x = new double[input1Discs];
        double[] y = new double[input2Discs];
        double[][] z = new double[y.length][x.length];
        double incrX, incrY;
        incrX = calcification.getDomain().getSize() / (input1Discs - 1.0);
        incrY = mass.getDomain().getSize() / (input2Discs - 1.0);

        // first, get the values
        for (int currentX = 0; currentX < input1Discs; currentX++) {
            x[currentX] = currentX * incrX;
        }
        for (int currentY = 0; currentY < input2Discs; currentY++) {
            y[currentY] = currentY * incrY;
        }

        for (int currentX = 0; currentX < input1Discs; currentX++) {
            calcification.setInput(x[currentX]);
            for (int currentY = 0; currentY < input2Discs; currentY++) {
                mass.setInput(y[currentY]);
                if (useCentroidDefuzzification)
                    output = rulebase.evaluate(1).get(impression);
                else
                    output = rulebase.evaluate(0).get(impression);
                z[currentY][currentX] = output;
            }
        }

        // now do the plotting
        JMathPlotter plotter = new JMathPlotter(17, 17, 14);
        plotter.plotControlSurface("Control Surface",
                new String[] { calcification.getName(), mass.getName(), impression.getName()}, x, y, z, impression.getDomain(), true);
        plotter.show("Type-1 Fuzzy Logic System Control Surface for Tipping Example");
    }

    public static void main(String args[]) {
        new BreastCancerMammogramDiagnosis();
    }
}
