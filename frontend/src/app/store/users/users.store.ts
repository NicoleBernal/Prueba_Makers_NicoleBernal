import { inject, Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { createActionGroup, createFeature, createReducer, emptyProps, on, props } from '@ngrx/store';
import { catchError, map, mergeMap, of, switchMap } from 'rxjs';
import { CreateUserPayload, UpdateUserPayload, User } from '../../shared/models/api.models';
import { UserApiService } from '../../core/services/api.services';

interface UsersState {
  users: User[];
  loading: boolean;
  error: string | null;
  confirmation: string | null;
}

const initialState: UsersState = { users: [], loading: false, error: null, confirmation: null };
const message = (error: unknown) => (error as { error?: { message?: string } })?.error?.message ?? 'La operación no pudo completarse.';

export const UserActions = createActionGroup({
  source: 'Users',
  events: {
    'Load': emptyProps(),
    'Load Success': props<{ users: User[] }>(),
    'Create': props<{ payload: CreateUserPayload }>(),
    'Update': props<{ id: number; payload: UpdateUserPayload }>(),
    'Save Success': props<{ user: User; confirmation: string }>(),
    'Delete': props<{ id: number }>(),
    'Delete Success': props<{ id: number }>(),
    'Failure': props<{ error: string }>(),
    'Clear Feedback': emptyProps()
  }
});

export const usersFeature = createFeature({
  name: 'users',
  reducer: createReducer(
    initialState,
    on(UserActions.load, UserActions.create, UserActions.update, UserActions.delete,
      state => ({ ...state, loading: true, error: null, confirmation: null })),
    on(UserActions.loadSuccess, (state, { users }) => ({ ...state, users, loading: false })),
    on(UserActions.saveSuccess, (state, { user, confirmation }) => ({
      ...state,
      users: state.users.some(item => item.id === user.id)
        ? state.users.map(item => item.id === user.id ? user : item)
        : [user, ...state.users],
      loading: false,
      confirmation
    })),
    on(UserActions.deleteSuccess, (state, { id }) => ({
      ...state,
      users: state.users.filter(user => user.id !== id),
      loading: false,
      confirmation: 'Usuario eliminado.'
    })),
    on(UserActions.failure, (state, { error }) => ({ ...state, loading: false, error })),
    on(UserActions.clearFeedback, state => ({ ...state, error: null, confirmation: null }))
  )
});

@Injectable()
export class UserEffects {
  private readonly actions$ = inject(Actions);
  private readonly api = inject(UserApiService);
  load$ = createEffect(() => this.actions$.pipe(ofType(UserActions.load), switchMap(() =>
    this.api.all().pipe(map(users => UserActions.loadSuccess({ users })), catchError(error => of(UserActions.failure({ error: message(error) })))))));
  create$ = createEffect(() => this.actions$.pipe(ofType(UserActions.create), mergeMap(({ payload }) =>
    this.api.create(payload).pipe(map(user => UserActions.saveSuccess({ user, confirmation: 'Usuario creado.' })), catchError(error => of(UserActions.failure({ error: message(error) })))))));
  update$ = createEffect(() => this.actions$.pipe(ofType(UserActions.update), mergeMap(({ id, payload }) =>
    this.api.update(id, payload).pipe(map(user => UserActions.saveSuccess({ user, confirmation: 'Usuario actualizado.' })), catchError(error => of(UserActions.failure({ error: message(error) })))))));
  delete$ = createEffect(() => this.actions$.pipe(ofType(UserActions.delete), mergeMap(({ id }) =>
    this.api.delete(id).pipe(map(() => UserActions.deleteSuccess({ id })), catchError(error => of(UserActions.failure({ error: message(error) })))))));
}
