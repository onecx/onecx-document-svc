package org.tkit.onecx.document.rs.internal.controllers;

import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import org.tkit.onecx.document.DocumentConfig;

import gen.org.tkit.onecx.document.rs.internal.SupportedMimeTypeControllerApi;

@ApplicationScoped
public class SupportedMimeTypeController implements SupportedMimeTypeControllerApi {

    @Inject
    DocumentConfig config;

    @Override
    public Response getAllSupportedMimeTypes() {
        var mimeTypes = Stream.of(config.supportedMimeTypes().split(","))
                .map(String::trim)
                .toList();
        return Response.status(Response.Status.OK)
                .entity(mimeTypes)
                .build();
    }

}
