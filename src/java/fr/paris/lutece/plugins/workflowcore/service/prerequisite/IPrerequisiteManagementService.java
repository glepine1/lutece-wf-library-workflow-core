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
package fr.paris.lutece.plugins.workflowcore.service.prerequisite;

import fr.paris.lutece.plugins.workflowcore.business.prerequisite.IPrerequisiteConfig;
import fr.paris.lutece.plugins.workflowcore.business.prerequisite.Prerequisite;

import java.util.List;

/**
 * Interface of the service that allows to manage prerequisites
 */
public interface IPrerequisiteManagementService
{
    /**
     * Get the list of prerequisites services
     * 
     * @return The list of prerequisite services
     */
    List<IAutomaticActionPrerequisiteService> getPrerequisiteServiceList( );

    /**
     * Get the prerequisite service associated with a prerequisite type
     * 
     * @param strPrerequisiteType
     *            The prerequisite type
     * @return The prerequisite service, or null if no service was found for the given prerequisite type
     */
    IAutomaticActionPrerequisiteService getPrerequisiteService( String strPrerequisiteType );

    /**
     * Get the list of prerequisites associated with an action
     * 
     * @param nIdAction
     *            The id of the action
     * @return The list of prerequisites associated with the given action, or an empty list if no prerequisite is associated with the given action
     */
    List<Prerequisite> getListPrerequisite( int nIdAction );

    /**
     * Get the configuration of a prerequisite
     * 
     * @param nIdPrerequisite
     *            the if of the prerequisite to get the configuration of
     * @param prerequisiteService
     *            The prerequisite service to use to get the configuration
     * @return The configuration, or null if the prerequisite has no configuration
     */
    IPrerequisiteConfig getPrerequisiteConfiguration( int nIdPrerequisite, IAutomaticActionPrerequisiteService prerequisiteService );
    
    /**
     * Copy the prerequisites from an action to another.
     * @param nIdActionSource the id of the source action
     * @param nIdActionTarget the id of the targetr action
     */
    void copyPrerequisite( int nIdActionSource, int nIdActionTarget );
    
    /**
     * Delete all the prerequisites of an action.
     * @param nIdAction
     */
    void deletePrerequisiteByAction( int nIdAction );
}
