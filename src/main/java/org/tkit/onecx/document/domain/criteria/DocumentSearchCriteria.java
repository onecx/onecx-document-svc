package org.tkit.onecx.document.domain.criteria;

import java.util.List;

import org.tkit.onecx.document.domain.models.enums.LifeCycleState;

import lombok.Getter;
import lombok.Setter;

/**
 * The Search Criteria for document.
 */
@Getter
@Setter
public class DocumentSearchCriteria {
    /**
     * The document id.
     */
    private String id;
    /**
     * The document name.
     */
    private String name;
    /**
     * The document state.
     */
    private List<LifeCycleState> lifeCycleState;
    /**
     * The document type id.
     */
    private List<String> documentTypeId;
    /**
     * The channel name.
     */
    private String channelName;
    /**
     * The number of page.
     */

    private String objectReferenceId;

    private String objectReferenceType;

    private Integer pageNumber;
    /**
     * The size of page.
     */
    private Integer pageSize;
}
