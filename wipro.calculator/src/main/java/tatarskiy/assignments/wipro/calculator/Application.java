package tatarskiy.assignments.wipro.calculator;

import java.io.IOException;
import java.time.YearMonth;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import tatarskiy.assignments.wipro.calculator.adjustments.AdjustmentDataProvider;
import tatarskiy.assignments.wipro.calculator.engine.CalculatorEngine;
import tatarskiy.assignments.wipro.calculator.engine.config.CalculatorConfig;
import tatarskiy.assignments.wipro.calculator.engine.impl.CalculatorEngineImpl;

@SpringBootApplication
public class Application implements CommandLineRunner {

  // Need to defer bean creation to when command line args are parsed and injected to
  // config properties.
  @Autowired
  @Lazy
  private Output output;

  @Autowired
  @Lazy
  private CalculatorDriver calculatorDriver;

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Bean
  CalculatorConfig calculatorConfig(@Autowired AggregatorBuilder aggregatorBuilder) {
    return new CalculatorConfig(
        aggregatorBuilder.adjustedTopTenRecentSumSupplier(),
        Map.of(
            "INSTRUMENT1", aggregatorBuilder.adjustedMedianSupplier(),
            "INSTRUMENT2", aggregatorBuilder.adjustedMonthlyMedianSupplier(YearMonth.of(2014, 10)),
            "INSTRUMENT3", aggregatorBuilder.adjustedMaxSupplier()
        )
    );
  }

  @Bean
  AdjustmentDataProvider adjustmentDataProvider() {
    return AdjustmentDataProvider.NO_OP_PROVIDER;
  }

  @Bean
  CalculatorEngine calculatorEngine(@Autowired CalculatorConfig calculatorConfig) {
    // TODO: filter working days
    return new CalculatorEngineImpl(calculatorConfig, x -> true);
  }

  @Override
  public void run(String... args) {
    // TODO: parse command line args
    // --silent => calculator.parallel =>
    // --pause=<sec>
    // --no_parallel => calculator.parallel => false
    // --no_h2_tcpserver => calculator
    // --h2_tcpserver_port=<h2_tcp_port>
    // --input=<input_file_path>
    try {
      CalculationResults results = calculatorDriver.runCalculation("example_input.txt");
      output.userMessage("Calculations complete, aggregate values are:");
      for (var aggregate : results.aggregatedValues()) {
        output.output(aggregate.getUserString());
      }
      output.userMessage(results.getProcessingTimeMessage());
      output.userMessage(results.getMemoryUsageMessage());
    } catch (IOException ioe) {
      output.error("I/O error when processing input data.", ioe);
    } catch (RuntimeException re) {
      output.error("Error when processing input data.", re);
    }
  }

}
