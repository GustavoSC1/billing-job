package com.gustavo.billingjob.config;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;

//A interface ItemProcessor no Spring Batch é usada para definir a lógica de processamento de itens durante a execução de um Step.
public class BillingDataProcessor implements ItemProcessor<BillingData, ReportingData> {
    
	@Value("${spring.cellular.spending.threshold:150}")
    private float spendingThreshold;
    
	private final PricingService pricingService;

    public BillingDataProcessor(PricingService pricingService) {
        this.pricingService = pricingService;
    }

	@Override
	public ReportingData process(BillingData item) {
		double billingTotal = item.dataUsage() * pricingService.getDataPricing() + item.callDuration() * pricingService.getCallPricing() + item.smsCount() * pricingService.getSmsPricing();
		if (billingTotal < spendingThreshold) {
			return null;
		}
		return new ReportingData(item, billingTotal);
	}
}