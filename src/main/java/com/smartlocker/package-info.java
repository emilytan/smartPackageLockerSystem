/**
 * Smart Package Locker Management System.
 *
 * <p>Where to start reading: {@code service.LockerService}. It has the four things the system can do
 * (create a locker, list lockers, store a parcel, collect a parcel) and calls everything else.
 *
 * <ul>
 *   <li>{@code model}: the business objects and their rules (locker, parcel, locker size)
 *   <li>{@code exception}: every way a request can fail
 *   <li>{@code contract}: interfaces, the things the service needs from outside
 *   <li>{@code implementation}: the real classes behind those interfaces
 *   <li>{@code service}: the entry point
 *   <li>{@code result}: the plain answers the service hands back
 * </ul>
 */
package com.smartlocker;
