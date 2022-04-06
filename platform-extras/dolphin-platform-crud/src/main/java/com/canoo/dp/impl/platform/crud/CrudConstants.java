package com.canoo.dp.impl.platform.crud;

import com.canoo.platform.remoting.server.event.Topic;

public interface CrudConstants {

    Topic<CrudEvent> CRUD_EVENT_TOPIC = Topic.create();
}
