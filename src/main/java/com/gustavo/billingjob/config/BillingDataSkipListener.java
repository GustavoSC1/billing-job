package com.gustavo.billingjob.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.springframework.batch.core.SkipListener;
import org.springframework.batch.item.file.FlatFileParseException;

// SkipListener é usado para monitorar eventos de "skip" durante o processamento de um job
// Nesse caso ele vai gravar as linhas puladas em um determinado arquivo
// <BillingData,BillingData> correspondem ao tipo de itens de entrada e saída do step em que esse listener será registrado
public class BillingDataSkipListener implements SkipListener<BillingData, BillingData> {
		
	Path skippedItemsFile;
	
	// Caminho para o arquivo no qual os itens ignorados devem ser gravados
	public BillingDataSkipListener(String skippedItemsFile) {
		this.skippedItemsFile = Paths.get(skippedItemsFile);
	}
	
	@Override
	public void onSkipInRead(Throwable throwable) {
		if(throwable instanceof FlatFileParseException exception) {
			String rawLine = exception.getInput();
			int lineNumber = exception.getLineNumber();
			String skippedLine = lineNumber + "|" + rawLine + System.lineSeparator();
			try {
				// StandardOpenOption.APPEND: Faz com que o conteúdo seja adicionado ao final do arquivo.
				// StandardOpenOption.CREATE: Cria o arquivo se ele não existir.
				Files.writeString(this.skippedItemsFile, skippedLine, StandardOpenOption.APPEND, StandardOpenOption.CREATE);				
			} catch(IOException e) {
				throw new RuntimeException("Unable to write skipped item " + skippedLine);
			}
		}	
	}

}
