package edu.fdu.se.core.miningactions.generator;

import edu.fdu.se.core.miningchangeentity.generator.ClusteredActionBean;

public class CreateEntity<T> {

    public T create(Class<T> c, ClusteredActionBean clusteredActionBean) {
        try {

            T s = c.getDeclaredConstructor(ClusteredActionBean.class).newInstance(clusteredActionBean);

            return s;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
