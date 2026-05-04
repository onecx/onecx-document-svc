package org.tkit.onecx.document;

import io.quarkus.runtime.annotations.ConfigDocFilename;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

/**
 * Document svc configuration
 */
@ConfigDocFilename("onecx-document-svc.adoc")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "onecx.document")
public interface DocumentConfig {

    /**
     * Supported MIME types for document uploads, separated by commas.
     */
    @WithName("supported-mime-types")
    String supportedMimeTypes();

}
