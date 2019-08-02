/*
 * Copyright (c) 2002-2014, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.workflowcore.service.task;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import fr.paris.lutece.plugins.workflowcore.service.config.ITaskConfigService;

/**
 *
 * ITaskService
 *
 */
public interface ITaskService
{
    /**
     * Creation of an instance of task
     * 
     * @param task
     *            The instance of task which contains the informations to store
     */
    void create( ITask task );

    /**
     * Update of task which is specified in parameter
     * 
     * @param task
     *            The instance of task which contains the informations to update
     */
    void update( ITask task );

    /**
     * Remove task which is specified in parameter
     * 
     * @param nIdTask
     *            The task key
     */
    void remove( int nIdTask );

    // /////////////////////////////////////////////////////////////////////////
    // Finders

    /**
     * Load the Task Object
     * 
     * @param nIdTask
     *            the task id
     * @param locale
     *            the locale
     * @return the Task Object
     */
    ITask findByPrimaryKey( int nIdTask, Locale locale );

    /**
     * Select all tasks associated to the action
     * 
     * @param nIdAction
     *            the action key
     * @param locale
     *            the locale
     * @return Task list
     */
    List<ITask> getListTaskByIdAction( int nIdAction, Locale locale );

    /**
     * return the maximum order number of the tasks in a given workflow
     * 
     * @param nIdAction
     *            the action id
     * @return the maximum order of the tasks in the given action
     */
    int findMaximumOrderByActionId( int nIdAction );

    /**
     * decrements the order of all the next tasks after the one which will be removed
     * 
     * @param nOrder
     *            the order id
     * @param nIdAction
     *            the action id
     */
    void decrementOrderByOne( int nOrder, int nIdAction );

    /**
     * Finds all the tasks which have an order lower to a given order
     * 
     * @param nOrder1
     *            the order 1
     * @param nOrder2
     *            the order 2
     * @param nIdAction
     *            the action id
     * @param locale
     *            the locale
     * @return List<ITask> the list of all the tasks
     */
    List<ITask> findTasksBetweenOrders( int nOrder1, int nOrder2, int nIdAction, Locale locale );

    /**
     * Finds all the tasks which have an order greater to a given order
     * 
     * @param nOrder
     *            the order
     * @param nIdAction
     *            the action id
     * @param locale
     *            the locale
     * @return List<ITask> the list of all the tasks
     */
    List<ITask> findTasksAfterOrder( int nOrder, int nIdAction, Locale locale );

    /**
     * Initialize the display order of tasks using their ids
     * 
     * @param nIdAction
     *            the action id
     * @param locale
     *            the locale
     */
    void initializeTaskOrder( int nIdAction, Locale locale );
    
    /**
     * Copy the task.
     * @param task
     */
    void copyTask( ITask task, List<ITaskConfigService> listTaskConfigService, Map<Integer, Integer> mapNewStates );
}
