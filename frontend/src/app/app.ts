import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { Store } from '@ngrx/store';
import { ToastHostComponent } from './shared/components/organisms/toast-host/toast-host.component';
import { AuthActions, authFeature } from './store/auth/auth.store';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, ToastHostComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class App {
  private readonly store = inject(Store);
  readonly user = this.store.selectSignal(authFeature.selectUser);
  logout(): void { this.store.dispatch(AuthActions.logout()); }
}
