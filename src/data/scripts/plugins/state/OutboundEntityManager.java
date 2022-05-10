package data.scripts.plugins.state;

import data.scripts.net.data.packables.APackable;

import java.util.List;
import java.util.Map;

public interface OutboundEntityManager {

    Map<Integer, ? extends APackable> getEntities();

    List<Integer> updateAndGetRemovedEntityInstanceIds();

}
