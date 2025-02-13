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
package org.thingsboard.server.dao.util.mapping;

import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.usertype.DynamicParameterizedType;

import java.util.Properties;

public abstract class AbstractArrayType<T>
        extends AbstractSingleColumnStandardBasicType<T>
        implements DynamicParameterizedType {

    public static final String SQL_ARRAY_TYPE = "sql_array_type";

    public AbstractArrayType(AbstractArrayTypeDescriptor<T> arrayTypeDescriptor) {
        super(
                ArraySqlTypeDescriptor.INSTANCE,
                arrayTypeDescriptor
        );
    }

    @Override
    protected boolean registerUnderJavaType() {
        return true;
    }

    @Override
    public void setParameterValues(Properties parameters) {
        ((AbstractArrayTypeDescriptor) getJavaTypeDescriptor()).setParameterValues(parameters);
    }
}
