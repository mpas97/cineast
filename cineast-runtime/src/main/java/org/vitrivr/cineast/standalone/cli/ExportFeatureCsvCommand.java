package org.vitrivr.cineast.standalone.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.features.abstracts.AbstractTextRetriever;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.standalone.config.Config;

import java.io.*;
import java.util.*;

/**
 * A CLI command that can be used to start a csv data export of a desired feature set.
 *
 * @author Maurizio Pasquinelli
 * @version 1.0
 */

@Command(name = "export", description = "Starts a csv data export of a desired feature set.")
public class ExportFeatureCsvCommand implements Runnable {

    @Option(name = {"-c", "--category"}, title = "Retriever category", description = "Name of the category where the feature retriever is placed in.")
    private String retrieverCategory;

    @Option(name = {"-n", "--name"}, title = "Retriever name", description = "Name of the feature retriever through which we should export all data.")
    private String retrieverName;

    @Override
    public void run() {
        List<String> categories = Config.sharedConfig().getRetriever().getRetrieverCategories();
        System.out.println("Available categories are: " + categories);
        if (categories.contains(retrieverCategory)) {
            System.out.println("Selecting retriever category: " + retrieverCategory);
            TObjectDoubleHashMap<Retriever> retrievers = Config.sharedConfig().getRetriever().getRetrieversByCategory(retrieverCategory);
            System.out.println("Retrievers in category: " +
                    Arrays.toString(Arrays.stream(retrievers.keys()).map(o -> o.getClass().getSimpleName()).toArray()));
            retrievers.forEachEntry((retriever, weight) -> {
                if (retriever.getClass().getSimpleName().equals(retrieverName)) {
                    System.out.println("= Retrieving for feature: " + retriever.getClass().getSimpleName() + " =");
                    retriever.init(Config.sharedConfig().getDatabase().getSelectorSupplier());
                    List<Map<String, PrimitiveTypeProvider>> list = null;
                    if (retriever instanceof AbstractFeatureModule) {
                        list = ((AbstractFeatureModule) retriever).getAll();
                    } else if (retriever instanceof AbstractTextRetriever) {
                        list = ((AbstractTextRetriever) retriever).getAll();
                    }
                    if (list != null) {
                        //System.out.println("mylog: listALL: length: " + list.size());
                        if (!list.isEmpty()) {
                            /*for (String key : list.get(0).keySet()) {
                                System.out.println("mylog: listALL: key: " + key);
                                System.out.println("mylog: listALL: value: " + Arrays.toString(list.get(0).get(key).getFloatArray()));
                            }*/
                            try {
                                PrintWriter writer = new PrintWriter("id_" + retrieverName + ".csv", "UTF-8");
                                for (Map<String, PrimitiveTypeProvider> item : list) {
                                    writer.println(
                                        item.get("id").toString().replaceAll("[StringPovdeImpl \\[au=\\]]", "")
                                                + ","
                                                + Arrays.toString(item.get("feature").getFloatArray())
                                                .replaceAll("[\\[ \\]]", ""));
                                }
                                writer.close();
                            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        System.out.println("Instanceof failed!");
                    }
                }
                return true;
            });
        } else {
            System.out.println("Category not found!");
        }
    }
}