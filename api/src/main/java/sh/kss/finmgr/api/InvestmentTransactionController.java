/*
    finmgr - a financial management framework
    Copyright (C) 2022  Kennedy Software Solutions Inc.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

    sean <at> kennedy <dot> software
 */
package sh.kss.finmgr.api;

import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.multipart.CompletedFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.kss.finmgr.domain.InvestmentTransaction;
import sh.kss.finmgr.service.CsvFileConverterService;
import sh.kss.finmgr.service.CsvFileConverterServiceImpl;
import sh.kss.finmgr.service.InvestmentTransactionService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

@Controller("/investment")
public class InvestmentTransactionController {

    private static final Logger log = LoggerFactory.getLogger(InvestmentTransactionController.class);

    private final InvestmentTransactionService transactionService;
    private final CsvFileConverterService csvService;

    public InvestmentTransactionController(
            InvestmentTransactionService transactionService,
            CsvFileConverterServiceImpl csvService
    ) {
        this.transactionService = transactionService;
        this.csvService = csvService;
    }

    @Get("/latest")
    List<InvestmentTransaction> latest() {
        log.info("Received request to /latest");

        return transactionService.getLatest();
    }

    @Post(value = "/import")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @SingleResult
    HttpResponse<String> importFile(@Part("file") CompletedFileUpload file) {
        try {
            File tempFile = File.createTempFile(file.getFilename(), "temp");
            Path path = Paths.get(tempFile.getAbsolutePath());
            Files.write(path, file.getBytes());
            try (Scanner scanner = new Scanner(tempFile)) {
                while (scanner.hasNext()) {
                    System.out.println(scanner.nextLine());
                }
            } catch (FileNotFoundException fnfe) {
                fnfe.printStackTrace();
            }
            return HttpResponse.ok("Uploaded");
        } catch (IOException e) {
            e.printStackTrace();
            return HttpResponse.badRequest("Upload Failed");
        }
    }
}