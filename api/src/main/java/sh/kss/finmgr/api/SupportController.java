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
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Part;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.multipart.CompletedFileUpload;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.kss.finmgr.service.FileImporterService;

import java.io.IOException;

@Controller("/support")
@AllArgsConstructor
public class SupportController {

    private static final Logger log = LoggerFactory.getLogger(SupportController.class);

    private final FileImporterService csvService;

    @Post(value = "/import")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @SingleResult
    HttpResponse<String> importFile(@Part("file") CompletedFileUpload file) {
        log.info("Received request to /import file");
        try {
            csvService.ingest(file.getBytes(), file.getFilename());
        } catch (IOException ioe) {
            return HttpResponse.badRequest(ioe.getMessage());
        }

        return HttpResponse.ok();
    }
}