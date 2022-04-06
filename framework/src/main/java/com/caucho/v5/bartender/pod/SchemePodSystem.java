/*
 * Copyright (c) 1998-2015 Caucho Technology -- all rights reserved
 *
 * This file is part of Baratine(TM)
 *
 * Each copy or derived work must preserve the copyright notice and this
 * notice unmodified.
 *
 * Baratine is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Baratine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, or any warranty
 * of NON-INFRINGEMENT.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Baratine; if not, write to the
 *
 *   Free Software Foundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Scott Ferguson
 */

package com.caucho.v5.bartender.pod;

import com.caucho.v5.amp.ServicesAmp;
import com.caucho.v5.amp.ServiceRefAmp;
import com.caucho.v5.bartender.BartenderSystem;

/**
 * Entry to the pod: scheme for system pods.
 */
public class SchemePodSystem extends SchemePod
{
  public SchemePodSystem(BartenderSystem bartender, ServicesAmp manager)
  {
    super(bartender, manager);
  }
  
  @Override
  protected ServiceRefAmp createPodRoot(PodBartender pod)
  {
    //ActorPodRoot actorPod = new ActorPodRootSystem(this, getManager(), pod);
    
    return new ServiceRefPodRoot(this, services(), pod, address());
    
    // return getManager().service(actorPod);
  }
  /*
  protected ServiceRefPodRoot createPodRoot(PodBartender pod)
  {
    return new ServiceRefPodRootSystem(this, getManager(), pod, getAddress());
  }
  */

  @Override
  public String getBartenderUrl(String serverId, String podName, int index)
  {
    //return "bartender://" + serverId + "/s/" + podName + '.' + index;
    return "bartender://" + serverId;
  }
}
