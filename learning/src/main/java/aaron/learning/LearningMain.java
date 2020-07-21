package aaron.learning;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.api.InvocationType;
import org.deeplearning4j.optimize.listeners.EvaluativeListener;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.cpu.nativecpu.blas.CpuLevel1;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.learning.config.Sgd;


public class LearningMain {

    private static final double LEARNING_RATE = 0.05;
    private static final int input = 100;
    private static final int out1 = 100;
    private static final int out2 = 100;
    private static final int out3 = 100;


    public static void main(String[] args) {

        DataSet testData=new DataSet();

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .weightInit(WeightInit.NORMAL)
                .activation(Activation.SIGMOID)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Sgd(0.05))
                .list()
                .layer(new DenseLayer.Builder().nIn(input).nOut(out1).build())
                .layer(new DenseLayer.Builder().nOut(out2).build())
                .layer(new DenseLayer.Builder().nOut(out3).build())
                .backpropType(BackpropType.Standard)
                .build();
        MultiLayerNetwork network = new MultiLayerNetwork(conf);
        network.init();
        network.setListeners(new ScoreIterationListener(10), new EvaluativeListener(testData, 1, InvocationType.EPOCH_END)); //Print score every 10 iterations and evaluate on test set every epoch
        network.fit(trainData, 10);

        //testing2

    }
}
