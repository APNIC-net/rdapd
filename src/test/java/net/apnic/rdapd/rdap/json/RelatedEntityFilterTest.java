package net.apnic.rdapd.rdap.json;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;

import net.apnic.rdapd.rdap.AutNum;
import net.apnic.rdapd.rdap.Entity;
import net.apnic.rdapd.rdap.RelatedEntity;
import net.apnic.rdapd.history.ObjectKey;
import net.apnic.rdapd.history.ObjectClass;

public class RelatedEntityFilterTest
{
    @Test
    public void testNestedEntityFiltering() throws Exception
    {
        /* First, confirm that nested entities are included by
         * default.  (The filter configured here does not affect the
         * results in this context.) */
        ObjectMapper mapper = new ObjectMapper();
        FilterProvider filterProvider =
            new SimpleFilterProvider()
                .addFilter("relatedEntitiesFilter",
                           SimpleBeanPropertyFilter.serializeAllExcept("_"));

        ObjectKey entityKey1 = new ObjectKey(ObjectClass.ENTITY, "E1");
        Entity entity1 = new Entity(entityKey1);
        RelatedEntity relatedEntity1 = new RelatedEntity(entityKey1, null, entity1);

        ObjectKey entityKey2 = new ObjectKey(ObjectClass.ENTITY, "E2");
        Entity entity2 = new Entity(entityKey2);
        RelatedEntity relatedEntity2 = new RelatedEntity(entityKey2, null, entity2);
        entity2.setRelatedEntities(Arrays.asList(relatedEntity1));

        ObjectKey objectKey = new ObjectKey(ObjectClass.AUT_NUM, "AS1234");
        AutNum autnum = new AutNum(objectKey);
        autnum.setASNInterval("1234", "1234");
        autnum.setRelatedEntities(Arrays.asList(relatedEntity1, relatedEntity2));

        String output = mapper.writer(filterProvider).writeValueAsString(autnum);

        Map<String, Object> autnumResult = mapper.readValue(output, Map.class);
        List<Map<String, Object>> entities =
            (List<Map<String, Object>>) autnumResult.get("entities");
        assertEquals("Got correct entity count", 2, entities.size());
 
        Map<String, Object> entity = (Map<String, Object>) entities.get(1);
        List<Object> nestedEntities = (List<Object>) entity.get("entities");
        assertEquals("Got correct nested entity count", 1, nestedEntities.size());

        /* Configure the filter, and confirm that nested entities are
         * no longer returned. */
        FilterProvider realFilterProvider =
            new SimpleFilterProvider()
                .addFilter("relatedEntitiesFilter",
                           new RelatedEntityFilter());

        output = mapper.writer(realFilterProvider).writeValueAsString(autnum);

        autnumResult = mapper.readValue(output, Map.class);
        entities = (List<Map<String, Object>>) autnumResult.get("entities");
        assertEquals("Got correct entity count", 2, entities.size());
 
        entity = (Map<String, Object>) entities.get(1);
        nestedEntities = (List<Object>) entity.get("entities");
        assertNull("No nested entities returned", nestedEntities);
    }
} 
