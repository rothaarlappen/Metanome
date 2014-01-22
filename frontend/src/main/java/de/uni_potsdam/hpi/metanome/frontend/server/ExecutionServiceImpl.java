package de.uni_potsdam.hpi.metanome.frontend.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import de.uni_potsdam.hpi.metanome.algorithm_execution.ProgressCache;
import de.uni_potsdam.hpi.metanome.algorithm_execution.TempFileGenerator;
import de.uni_potsdam.hpi.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.uni_potsdam.hpi.metanome.algorithm_integration.AlgorithmExecutionException;
import de.uni_potsdam.hpi.metanome.algorithm_integration.algorithm_execution.FileGenerator;
import de.uni_potsdam.hpi.metanome.algorithm_integration.input.SQLInputGenerator;
import de.uni_potsdam.hpi.metanome.algorithm_integration.result_receiver.OmniscientResultReceiver;
import de.uni_potsdam.hpi.metanome.algorithm_integration.results.Result;
import de.uni_potsdam.hpi.metanome.algorithm_loading.AlgorithmExecutor;
import de.uni_potsdam.hpi.metanome.algorithm_loading.AlgorithmLoadingException;
import de.uni_potsdam.hpi.metanome.configuration.ConfigurationValue;
import de.uni_potsdam.hpi.metanome.configuration.ConfigurationValueBoolean;
import de.uni_potsdam.hpi.metanome.configuration.ConfigurationValueRelationalInputGenerator;
import de.uni_potsdam.hpi.metanome.configuration.ConfigurationValueSQLInputGenerator;
import de.uni_potsdam.hpi.metanome.configuration.ConfigurationValueString;
import de.uni_potsdam.hpi.metanome.frontend.client.parameter.InputParameter;
import de.uni_potsdam.hpi.metanome.frontend.client.parameter.InputParameterBoolean;
import de.uni_potsdam.hpi.metanome.frontend.client.parameter.InputParameterCsvFile;
import de.uni_potsdam.hpi.metanome.frontend.client.parameter.InputParameterSQLIterator;
import de.uni_potsdam.hpi.metanome.frontend.client.parameter.InputParameterString;
import de.uni_potsdam.hpi.metanome.frontend.client.services.ExecutionService;
import de.uni_potsdam.hpi.metanome.input.csv.CsvFileGenerator;
import de.uni_potsdam.hpi.metanome.input.sql.SqlIteratorGenerator;
import de.uni_potsdam.hpi.metanome.result_receiver.ResultPrinter;
import de.uni_potsdam.hpi.metanome.result_receiver.ResultsCache;
import de.uni_potsdam.hpi.metanome.result_receiver.ResultsHub;

/**
 * Service Implementation for service that triggers algorithm execution
 */
public class ExecutionServiceImpl extends RemoteServiceServlet implements ExecutionService {
	
	private static final long serialVersionUID = -2758103927345131933L;
	
	protected HashMap<String, ResultsCache> currentResultReceiver = new HashMap<String, ResultsCache>();
	protected HashMap<String, ProgressCache> currentProgressCaches = new HashMap<String, ProgressCache>();
	
	/**
	 * Builds an {@link AlgorithmExecutor} with stacked {@link OmniscientResultReceiver}s to write result files and 
	 * cache results for the frontend.
	 * 
	 * @param algorithmName
	 * @param executionIdentifier
	 * 
	 * @return an {@link AlgorithmExecutor}
	 * 
	 * @throws FileNotFoundException 
	 * @throws UnsupportedEncodingException 
	 * 
	 */
	protected AlgorithmExecutor buildExecutor(String algorithmName, String executionIdentifier) throws FileNotFoundException, UnsupportedEncodingException {		
		ResultPrinter resultPrinter = new ResultPrinter(executionIdentifier, "results");
		ResultsCache resultsCache = new ResultsCache();
		ResultsHub resultsHub = new ResultsHub();
		resultsHub.addSubscriber(resultPrinter);
		resultsHub.addSubscriber(resultsCache);
		
		FileGenerator fileGenerator = new TempFileGenerator();
		
		ProgressCache progressCache = new ProgressCache();
		
		AlgorithmExecutor executor = new AlgorithmExecutor(resultsHub, progressCache, fileGenerator);
		currentResultReceiver.put(executionIdentifier, resultsCache);
		currentProgressCaches.put(executionIdentifier, progressCache);
		return executor;
	}
	
	private List<ConfigurationValue> convertInputParameters(
			List<InputParameter> parameters) throws AlgorithmConfigurationException {
		List<ConfigurationValue> configValuesList = new LinkedList<ConfigurationValue>();
		for (InputParameter parameter : parameters){
			ConfigurationValue configValue = convertToConfigurationValue(parameter);
			configValuesList.add(configValue);
		}
		return configValuesList;
	}

	/**
	 * TODO docs
	 * 
	 * @param parameter
	 * @return
	 * @throws AlgorithmConfigurationException
	 */
	public ConfigurationValue convertToConfigurationValue(
			InputParameter parameter) throws AlgorithmConfigurationException {
		//TODO all types of ConfigurationValues
		if (parameter instanceof InputParameterString)
			return new ConfigurationValueString(parameter.getIdentifier(), 
					((InputParameterString) parameter).getValue());
		
		else if (parameter instanceof InputParameterBoolean)
			return new ConfigurationValueBoolean(parameter.getIdentifier(), 
					((InputParameterBoolean) parameter).getValue());
		
		else if (parameter instanceof InputParameterCsvFile)
			return new ConfigurationValueRelationalInputGenerator(parameter.getIdentifier(), 
					buildCsvFileGenerator((InputParameterCsvFile) parameter));
		
		else if (parameter instanceof InputParameterSQLIterator)
			return new ConfigurationValueSQLInputGenerator(parameter.getIdentifier(), 
					buildSQLInputGenerator((InputParameterSQLIterator) parameter));
		
		else
			return null;
	}

	/**
	 * TODO docs
	 * 
	 * @param parameter
	 * @return
	 * @throws AlgorithmConfigurationException
	 */
	private SQLInputGenerator buildSQLInputGenerator(
			InputParameterSQLIterator parameter) throws AlgorithmConfigurationException {
		return new SqlIteratorGenerator(parameter.getDbUrl(), parameter.getUserName(), parameter.getPassword());
	}

	/**
	 * TODO docs
	 * 
	 * @param param
	 * @return
	 * @throws AlgorithmConfigurationException
	 */
	protected CsvFileGenerator buildCsvFileGenerator(InputParameterCsvFile param) throws AlgorithmConfigurationException {
		try {
			if (param.isAdvanced())
				return new CsvFileGenerator(new File(param.getFileNameValue()), param.getSeparatorChar(), 
						param.getQuoteChar(), param.getEscapeChar(), param.getLine(), 
						param.isStrictQuotes(), param.isIgnoreLeadingWhiteSpace()) ;
			else
				return new CsvFileGenerator(new File(param.getFileNameValue()));
		} catch (FileNotFoundException e) {
			throw new AlgorithmConfigurationException("Error opening specified CSV file.");		
		}
	}	
	
	@Override
	public long executeAlgorithm(String algorithmName, String executionIdentifier, List<InputParameter> parameters) throws AlgorithmConfigurationException, AlgorithmLoadingException, AlgorithmExecutionException {
		List<ConfigurationValue> configs = convertInputParameters(parameters);
		AlgorithmExecutor executor = null;
		
		try {
			executor = buildExecutor(algorithmName, executionIdentifier);
		} catch (FileNotFoundException e) {
			throw new AlgorithmExecutionException("Could not generate result file.");
		} catch (UnsupportedEncodingException e) {
			throw new AlgorithmExecutionException("Could not build temporary file generator.");
		}
		long executionTime = executor.executeAlgorithm(algorithmName, configs);
		try {
			executor.close();
		} catch (IOException e) {
			throw new AlgorithmExecutionException("Could not close algorithm executor.");
		}
		
		return executionTime;
	}
	
	@Override
	public ArrayList<Result> fetchNewResults(String executionIdentifier){	
		// FIXME return exception when algorithm name is not in map
		return currentResultReceiver.get(executionIdentifier).getNewResults();
	}

	@Override
	public float fetchProgress(String executionIdentifier) {
		// FIXME return exception when algorithm name is not in map
		return currentProgressCaches.get(executionIdentifier).getProgress();
	}
}
