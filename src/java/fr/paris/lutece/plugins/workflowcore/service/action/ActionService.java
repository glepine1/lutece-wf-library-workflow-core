/*
 * Copyright (c) 2002-2012, Mairie de Paris
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
package fr.paris.lutece.plugins.workflowcore.service.action;

import fr.paris.lutece.plugins.workflowcore.business.action.Action;
import fr.paris.lutece.plugins.workflowcore.business.action.ActionFilter;
import fr.paris.lutece.plugins.workflowcore.business.action.IActionDAO;
import fr.paris.lutece.plugins.workflowcore.business.resource.ResourceHistory;
import fr.paris.lutece.plugins.workflowcore.service.resource.IResourceHistoryService;
import fr.paris.lutece.plugins.workflowcore.service.task.ITask;
import fr.paris.lutece.plugins.workflowcore.service.task.ITaskService;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;


/**
 *
 * ActionService
 *
 */
public class ActionService implements IActionService
{
    public static final String BEAN_SERVICE = "workflow.actionService";
    @Inject
    private IActionDAO _actionDAO;
    @Inject
    private IResourceHistoryService _resourceHistoryService;
    @Inject
    private ITaskService _taskService;

    /**
     * {@inheritDoc}
     */
    @Override
    public void create( Action action )
    {
        _actionDAO.insert( action );
        createLinkedActions( action );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update( Action action )
    {
        _actionDAO.store( action );
        removeLinkedActions( action.getId(  ) );
        createLinkedActions( action );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove( int nIdAction )
    {
        Action action = findByPrimaryKey( nIdAction );

        if ( action != null )
        {
            //remove resource history associated
            List<ResourceHistory> listResourceHistory = _resourceHistoryService.getAllHistoryByAction( nIdAction );

            for ( ResourceHistory resourceHistory : listResourceHistory )
            {
                _resourceHistoryService.remove( resourceHistory.getId(  ) );
            }
        }

        //remove all task associated with the action
        List<ITask> listTask = _taskService.getListTaskByIdAction( nIdAction, Locale.FRENCH );

        for ( ITask task : listTask )
        {
            task.doRemoveConfig(  );
            _taskService.remove( task.getId(  ) );
        }

        removeLinkedActions( nIdAction );

        _actionDAO.delete( nIdAction );
    }

    // FINDERS

    /**
     * {@inheritDoc}
     */
    @Override
    public Action findByPrimaryKey( int nIdAction )
    {
        Action action = _actionDAO.loadWithIcon( nIdAction );
        action.setListIdsLinkedAction( getListIdsLinkedAction( nIdAction ) );

        return action;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Action> getListActionByFilter( ActionFilter filter )
    {
        List<Action> listActions = _actionDAO.selectActionsByFilter( filter );

        if ( ( listActions != null ) && !listActions.isEmpty(  ) )
        {
            for ( Action action : listActions )
            {
                action.setListIdsLinkedAction( getListIdsLinkedAction( action.getId(  ) ) );
            }
        }

        return listActions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createLinkedActions( Action action )
    {
        if ( ( action != null ) && ( action.getListIdsLinkedAction(  ) != null ) &&
                !action.getListIdsLinkedAction(  ).isEmpty(  ) )
        {
            for ( int nIdLinkedAction : action.getListIdsLinkedAction(  ) )
            {
                _actionDAO.insertLinkedActions( action.getId(  ), nIdLinkedAction );
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Integer> getListIdsLinkedAction( int nIdAction )
    {
        return _actionDAO.selectListIdsLinkedAction( nIdAction );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeLinkedActions( int nIdAction )
    {
        _actionDAO.removeLinkedActions( nIdAction );
    }
}