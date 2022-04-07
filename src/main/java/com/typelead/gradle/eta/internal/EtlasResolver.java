package com.typelead.gradle.eta.internal;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.gradle.api.GradleException;
import org.gradle.api.Nullable;
import org.gradle.api.Project;

import com.typelead.gradle.utils.Arch;
import com.typelead.gradle.utils.CommandLine;
import com.typelead.gradle.utils.Either;
import com.typelead.gradle.utils.EtlasCommand;
import com.typelead.gradle.utils.ResolvedExecutable;
import com.typelead.gradle.utils.SystemPathUtil;

/**
 * Manages resolving an Etlas binary by either
 * <ul>
 * <li>Locating one on the system PATH</li>
 * <li>Using a configured local installation</li>
 * <li>Downloading one from the repository</li>
 * </ul>
 */
public class EtlasResolver {

    private String cacheDir;

    public EtlasResolver(String cacheDir) {
        this.cacheDir = cacheDir;
    }

    public ResolvedExecutable resolveInSystemPath() {
        String execExt = getArch().fold(x -> "", arch -> arch.execExt);
        File etlas = SystemPathUtil.findExecutable("etlas" + execExt);
        if (etlas == null) {
            throw new GradleException("Could not find etlas executable on system PATH.");
        }
        String etlasPath;
        try {
            etlasPath = etlas.getCanonicalPath();
        } catch (IOException e) {
            throw new GradleException("Failed to get canonical path for etlas '" + etlas.getPath() + "'", e);
        }
        return new ResolvedExecutable(etlasPath, null, true);
    }

    public ResolvedExecutable resolveLocalPath(String etlasPath) {
        File etlas = new File(etlasPath);
        if (!etlas.canExecute()) {
            throw new GradleException("Provided etlas binary is not executable: " + etlasPath);
        }
        return new ResolvedExecutable(etlasPath, null, true);
    }

    public ResolvedExecutable resolveRemote(String repo, String version) {
        Arch arch = getArch().valueOr(e -> { throw e; });
        EtlasCache cache = new EtlasCache(cacheDir);
        String etlasPath = cache.getBinaryPathForVersion(version, arch);
        boolean fresh = false;
        if (etlasPath == null) {
            etlasPath = cache.putBinaryForVersion(version, getEtlasUrl(repo, version, arch), arch);
            fresh = true;
        }
        return new ResolvedExecutable(etlasPath, version, false, fresh);
    }

    private String getEtlasUrlString(String repo, String version, Arch arch) {
        return repo + "/etlas-" + version + "/binaries/" +
               arch.name + "/etlas" + arch.execExt;
    }

    private URL getEtlasUrl(String repo, String version, Arch arch) {
        try {
            return new URL(getEtlasUrlString(repo, version, arch));
        } catch (MalformedURLException e) {
            throw new GradleException("Malformed etlasRepo '" + repo + "'", e);
        }
    }

    private Either<GradleException, Arch> getArch() {
        return Arch.current()
            .leftMap(os -> new GradleException
                     ("Unsupported OS '" + os +
                      "'; install etlas manually and configure with etlasPath"));
    }
}
