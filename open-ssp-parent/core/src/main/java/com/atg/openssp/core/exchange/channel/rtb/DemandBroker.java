package com.atg.openssp.core.exchange.channel.rtb;

import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atg.openssp.common.core.broker.AbstractBroker;
import com.atg.openssp.common.core.entry.SessionAgent;
import com.atg.openssp.common.demand.ResponseContainer;
import com.atg.openssp.common.demand.Supplier;
import com.atg.openssp.common.exception.BidProcessingException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import openrtb.bidrequest.model.BidRequest;
import openrtb.bidresponse.model.BidResponse;

/**
 * This class acts as Broker to a connector used in demand (OpenRTB) context. It represents one Demand side (DSP).
 * 
 * @author André Schmer
 *
 */
public final class DemandBroker extends AbstractBroker implements Callable<ResponseContainer> {

	private static final Logger log = LoggerFactory.getLogger(DemandBroker.class);

	private final Supplier supplier;

	private final OpenRtbConnector connector;

	private final Header[] headers;

	private final Gson gson;

	private BidRequest bidrequest;

	public DemandBroker(final Supplier supplier, final OpenRtbConnector connector, final SessionAgent agent) {
		sessionAgent = agent;
		this.supplier = supplier;
		this.connector = connector;

		headers = new Header[2];
		headers[0] = new BasicHeader("x-openrtb-version", supplier.getOpenRtbVersion());
		headers[1] = new BasicHeader("ContentType", supplier.getContentType());
		// headers[2] = new BasicHeader("Accept-Encoding", supplier.getAcceptEncoding());
		// headers[3] = new BasicHeader("Content-Encoding", supplier.getContentEncoding());

		gson = new GsonBuilder().setVersion(Double.valueOf(supplier.getOpenRtbVersion())).create();
	}

	@Override
	public ResponseContainer call() throws Exception {
		if (bidrequest == null) {
			return null;
		}

		try {
			final String jsonBidrequest = gson.toJson(bidrequest, BidRequest.class);
			log.debug("biderquest: " + jsonBidrequest);
			final String result = connector.connect(jsonBidrequest, headers);
			if (!StringUtils.isEmpty(result)) {
				log.debug("bidresponse: " + result);

				final BidResponse bidResponse = gson.fromJson(result, BidResponse.class);

				return new ResponseContainer(supplier, bidResponse);
			}
		} catch (final BidProcessingException e) {
			log.error(getClass().getSimpleName() + " " + e.getMessage());
		} catch (final Exception e) {
			log.error(getClass().getSimpleName() + " " + e.getMessage());
		}
		return null;
	}

	public void setBidRequest(final BidRequest bidrequest) {
		this.bidrequest = bidrequest;
	}

}
