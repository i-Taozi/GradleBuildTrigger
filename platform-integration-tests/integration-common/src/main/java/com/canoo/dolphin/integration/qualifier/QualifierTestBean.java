/*
 * Copyright 2015-2018 Canoo Engineering AG.
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
package com.canoo.dolphin.integration.qualifier;

import com.canoo.platform.remoting.RemotingBean;
import com.canoo.platform.remoting.Property;

@RemotingBean
public class QualifierTestBean {

    private Property<QualifierTestSubBean> qualifierTestSubBeanOneValue;

    private Property<QualifierTestSubBean> qualifierTestSubBeanTwoValue;

    public QualifierTestSubBean getQualifierTestSubBeanOneValue() {
        return qualifierTestSubBeanOneValue.get();
    }

    public Property<QualifierTestSubBean> qualifierTestSubBeanOneValueProperty() {
        return qualifierTestSubBeanOneValue;
    }

    public void setQualifierTestSubBeanOneValue(QualifierTestSubBean qualifierTestSubBeanValue) {
        this.qualifierTestSubBeanOneValue.set(qualifierTestSubBeanValue);
    }

    public QualifierTestSubBean getQualifierTestSubBeanTwoValue() {
        return qualifierTestSubBeanTwoValue.get();
    }

    public Property<QualifierTestSubBean> qualifierTestSubBeanTwoValueProperty() {
        return qualifierTestSubBeanTwoValue;
    }

    public void setQualifierTestSubBeanTwoValue(QualifierTestSubBean qualifierTestSubBeanTwoValue) {
        this.qualifierTestSubBeanTwoValue.set(qualifierTestSubBeanTwoValue);
    }
}

