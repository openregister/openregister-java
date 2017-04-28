package uk.gov.register.service;

import org.glassfish.hk2.api.IterableProvider;
import org.jvnet.hk2.annotations.Service;
import uk.gov.register.indexer.function.IndexFunction;

import javax.inject.Inject;

@Service
public class IndexService {
    private IterableProvider<IndexFunction> indexFunctions;

    @Inject
    public IndexService(IterableProvider<IndexFunction> indexFunctions) {
        this.indexFunctions = indexFunctions;
    }

    public void test() {
        int size = indexFunctions.getSize();
        String a = "does nothing";
    }
}
