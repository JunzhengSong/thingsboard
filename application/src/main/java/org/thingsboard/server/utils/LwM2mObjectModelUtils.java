/**
 * Copyright © 2016-2023 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.server.utils;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.leshan.core.model.InvalidDDFFileException;
import org.eclipse.leshan.core.model.ObjectModel;
import org.thingsboard.server.common.data.ResourceType;
import org.thingsboard.server.common.data.TbResource;
import org.thingsboard.server.common.data.exception.ThingsboardErrorCode;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.lwm2m.LwM2mInstance;
import org.thingsboard.server.common.data.lwm2m.LwM2mObject;
import org.thingsboard.server.common.data.lwm2m.LwM2mResourceObserve;
import org.thingsboard.server.common.data.util.TbDDFFileParser;
import org.thingsboard.server.dao.exception.DataValidationException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.thingsboard.server.common.data.lwm2m.LwM2mConstants.LWM2M_SEPARATOR_KEY;
import static org.thingsboard.server.common.data.lwm2m.LwM2mConstants.LWM2M_SEPARATOR_SEARCH_TEXT;

// 处理LwM2M（Lightweight M2M）对象模型（Object Model）的工具类
@Slf4j
public class LwM2mObjectModelUtils {

    private static final TbDDFFileParser ddfFileParser = new TbDDFFileParser();

    //toLwm2mResource(TbResource resource) 方法：将TbResource对象转换为LwM2M资源对象。它通过解析TbResource中存储的XML数据，生成LwM2M对象模型，
    // 并设置相应的属性，如资源键（resourceKey）、标题（title）、搜索文本（searchText）等。如果解析失败，会抛出相关异常。
    public static void toLwm2mResource (TbResource resource) throws ThingsboardException {
        try {
            //通过Base64.getDecoder().decode(resource.getData())将resource中的Base64编码的数据解码为字节数组。
            //通过parse转换成objectModel列表
            List<ObjectModel> objectModels =
                    ddfFileParser.parse(new ByteArrayInputStream(Base64.getDecoder().decode(resource.getData())), resource.getSearchText());
            if (!objectModels.isEmpty()) {
                ObjectModel objectModel = objectModels.get(0);

                String resourceKey = objectModel.id + LWM2M_SEPARATOR_KEY + objectModel.version;
                String name = objectModel.name;
                resource.setResourceKey(resourceKey);
                if (resource.getId() == null) {
                    resource.setTitle(name + " id=" + objectModel.id + " v" + objectModel.version);
                }
                resource.setSearchText(resourceKey + LWM2M_SEPARATOR_SEARCH_TEXT + name);
            } else {
                throw new DataValidationException(String.format("Could not parse the XML of objectModel with name %s", resource.getSearchText()));
            }
        } catch (InvalidDDFFileException e) {
            log.error("Failed to parse file {}", resource.getFileName(), e);
            throw new DataValidationException("Failed to parse file " + resource.getFileName());
        } catch (IOException e) {
            throw new ThingsboardException(e, ThingsboardErrorCode.GENERAL);
        }
        if (resource.getResourceType().equals(ResourceType.LWM2M_MODEL) && toLwM2mObject(resource, true) == null) {
            throw new DataValidationException(String.format("Could not parse the XML of objectModel with name %s", resource.getSearchText()));
        }
    }

    public static LwM2mObject toLwM2mObject(TbResource resource, boolean isSave) {
        try {
            List<ObjectModel> objectModels =
                    ddfFileParser.parse(new ByteArrayInputStream(Base64.getDecoder().decode(resource.getData())), resource.getSearchText());
            if (objectModels.size() == 0) {
                return null;
            } else {
                ObjectModel obj = objectModels.get(0);
                LwM2mObject lwM2mObject = new LwM2mObject();
                lwM2mObject.setId(obj.id);
                lwM2mObject.setKeyId(resource.getResourceKey());
                lwM2mObject.setName(obj.name);
                lwM2mObject.setMultiple(obj.multiple);
                lwM2mObject.setMandatory(obj.mandatory);
                LwM2mInstance instance = new LwM2mInstance();
                instance.setId(0);
                List<LwM2mResourceObserve> resources = new ArrayList<>();
                // resources是map，使用foreach遍历，对里面的每个元素 （k,v)用lambda表达式处理
                obj.resources.forEach((k, v) -> {
                    if (isSave) {
                        LwM2mResourceObserve lwM2MResourceObserve = new LwM2mResourceObserve(k, v.name, false, false, false);
                        resources.add(lwM2MResourceObserve);
                    } else if (v.operations.isReadable()) {
                        LwM2mResourceObserve lwM2MResourceObserve = new LwM2mResourceObserve(k, v.name, false, false, false);
                        resources.add(lwM2MResourceObserve);
                    }
                });
                if (isSave || resources.size() > 0) {
                    instance.setResources(resources.toArray(LwM2mResourceObserve[]::new));
                    lwM2mObject.setInstances(new LwM2mInstance[]{instance});
                    return lwM2mObject;
                } else {
                    return null;
                }
            }
        } catch (IOException | InvalidDDFFileException e) {
            log.error("Could not parse the XML of objectModel with name [{}]", resource.getSearchText(), e);
            return null;
        }
    }

}
