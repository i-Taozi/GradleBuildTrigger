/*
 * Copyright 2016 Greg Whitaker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.gregwhitaker.catnap.core.view;

import com.github.gregwhitaker.catnap.core.context.CatnapContext;
import com.github.gregwhitaker.catnap.core.context.HttpStatus;
import com.github.gregwhitaker.catnap.core.model.builder.ModelBuilder;
import com.github.gregwhitaker.catnap.core.query.builder.QueryBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

/**
 * Base view class that all Catnap views must extend.
 */
public abstract class CatnapView {
    protected QueryBuilder queryBuilder;
    protected ModelBuilder modelBuilder;

    protected CatnapView(QueryBuilder queryBuilder, ModelBuilder modelBuilder) {
        this.queryBuilder = queryBuilder;
        this.modelBuilder = modelBuilder;
    }

    /**
     * Renders a successful response.
     *
     * @param value        model object to render
     * @param outputStream response outputstream to write the rendered response
     * @param context      context information used during rendering of the response
     * @throws Exception
     */
    protected abstract void render(Object value, OutputStream outputStream, CatnapContext context) throws Exception;

    /**
     * Renders an error response.
     *
     * @param value        model object to render
     * @param outputStream response outputstream to write the rendered response
     * @param context      context information used during rendering of the response
     * @throws Exception
     */
    protected abstract void renderError(Object value, OutputStream outputStream, CatnapContext context) throws Exception;

    /**
     * Renders a response.
     *
     * @param request  {@link javax.servlet.http.HttpServletRequest} to use during rendering
     * @param response {@link javax.servlet.http.HttpServletResponse} to use during rendering
     * @param value    model object to render
     * @throws Exception
     */
    public final void render(HttpServletRequest request, HttpServletResponse response, Object value) throws Exception {
        render(value, response.getOutputStream(), new CatnapContext(request, response, queryBuilder));
    }

    /**
     * Renders a response with the specified HTTP status.
     *
     * @param request    {@link javax.servlet.http.HttpServletRequest} to use during rendering
     * @param response   {@link javax.servlet.http.HttpServletResponse} to use during rendering
     * @param value      model object to render
     * @param httpStatus http status to return on the response
     * @throws Exception
     */
    public final void render(HttpServletRequest request, HttpServletResponse response, Object value, HttpStatus httpStatus) throws Exception {
        if (httpStatus == HttpStatus.OK) {
            render(value, response.getOutputStream(), new CatnapContext(request, response, queryBuilder, httpStatus));
        } else {
            renderError(value, response.getOutputStream(), new CatnapContext(request, response, queryBuilder, httpStatus));
        }
    }

    /**
     * @return content type returned by the view (ex. "application/json")
     */
    public abstract String getContentType();

    /**
     * @return character encoding returned by the view (ex. "UTF-8")
     */
    public String getEncoding() {
        return "UTF-8";
    }

    /**
     * @return suffix on the end of the href that triggers the view (ex. ".json")
     */
    public abstract String getHrefSuffix();

    /**
     * @return query builder associated with the view
     */
    public QueryBuilder getQueryBuilder() {
        return queryBuilder;
    }
}
