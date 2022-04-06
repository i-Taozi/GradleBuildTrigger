/*
 * Copyright 2006-2018 The MZmine 2 Development Team
 * 
 * This file is part of MZmine 2.
 * 
 * MZmine 2 is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * MZmine 2 is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with MZmine 2; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
 * USA
 */

package net.sf.mzmine.modules.peaklistmethods.identification.onlinedbsearch;

import javax.annotation.Nonnull;

import net.sf.mzmine.modules.MZmineModule;
import net.sf.mzmine.modules.peaklistmethods.identification.onlinedbsearch.databases.ChemSpiderGateway;
import net.sf.mzmine.modules.peaklistmethods.identification.onlinedbsearch.databases.ChemSpiderParameters;
import net.sf.mzmine.modules.peaklistmethods.identification.onlinedbsearch.databases.HMDBGateway;
import net.sf.mzmine.modules.peaklistmethods.identification.onlinedbsearch.databases.KEGGGateway;
import net.sf.mzmine.modules.peaklistmethods.identification.onlinedbsearch.databases.LipidMapsGateway;
import net.sf.mzmine.modules.peaklistmethods.identification.onlinedbsearch.databases.MassBankEuropeGateway;
import net.sf.mzmine.modules.peaklistmethods.identification.onlinedbsearch.databases.MetaCycGateway;
import net.sf.mzmine.modules.peaklistmethods.identification.onlinedbsearch.databases.PubChemGateway;
import net.sf.mzmine.modules.peaklistmethods.identification.onlinedbsearch.databases.YMDBGateway;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.parameters.impl.SimpleParameterSet;

public enum OnlineDatabases implements MZmineModule {

  KEGG("KEGG", KEGGGateway.class), //
  PubChem("PubChem", PubChemGateway.class), //
  HMDB("Human Metabolome (HMDB)", HMDBGateway.class), //
  YMDB("Yeast Metabolome (YMDB)", YMDBGateway.class), //
  // METLIN("METLIN Database", MetLinGateway.class, MetLinParameters.class),
  LIPIDMAPS("LipidMaps", LipidMapsGateway.class), //
  // MASSBANKJapan("Japanese MassBank", MassBankJapanGateway.class), //
  MASSBANKEurope("MassBank.eu", MassBankEuropeGateway.class), //
  CHEMSPIDER("ChemSpider", ChemSpiderGateway.class, ChemSpiderParameters.class), //
  METACYC("MetaCyc", MetaCycGateway.class);

  private final @Nonnull String dbName;
  private final @Nonnull Class<? extends DBGateway> gatewayClass;
  private final @Nonnull Class<? extends ParameterSet> parametersClass;

  OnlineDatabases(final @Nonnull String dbName,
      final @Nonnull Class<? extends DBGateway> gatewayClass,
      final @Nonnull Class<? extends ParameterSet> parametersClass) {
    this.dbName = dbName;
    this.gatewayClass = gatewayClass;
    this.parametersClass = parametersClass;
  }

  OnlineDatabases(final @Nonnull String name,
      final @Nonnull Class<? extends DBGateway> gatewayClass) {
    this(name, gatewayClass, SimpleParameterSet.class);
  }

  public Class<? extends DBGateway> getGatewayClass() {
    return gatewayClass;
  }

  public @Nonnull String getName() {
    return dbName;
  }

  @Override
  public @Nonnull Class<? extends ParameterSet> getParameterSetClass() {
    return parametersClass;
  }
}
