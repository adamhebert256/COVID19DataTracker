package edu.upenn.cit594.processor;

import edu.upenn.cit594.util.PropertyData;

public class MarketValueSelector implements Selector {

	@Override
	public Double getData(PropertyData data) {
		return data.getMarketValue();
	}

}
