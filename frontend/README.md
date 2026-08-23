# EKC Console

The web console for the Engineering Knowledge Compiler. It submits Git repositories to the compiler API, monitors API health, explains the compilation pipeline, and stores the latest 20 run results in the current browser.

## Prerequisites

- Node.js 22 or newer
- npm 10 or newer
- The EKC Spring Boot API running on port `8080`

## Run locally

From the application repository, use two terminals:

```bash
# Terminal 1: backend
mvn spring-boot:run

# Terminal 2: frontend
cd frontend
npm install
npm run dev
```

Open <http://localhost:5173>. Vite forwards `/api` and `/health` requests to `http://localhost:8080`, so local development does not require CORS configuration.

## Useful commands

```bash
npm run dev       # start the development server
npm run build     # type-check and create the production bundle
npm run lint      # run ESLint
npm run test      # run the unit tests once
npm run preview   # preview the production bundle
```

## Production image

```bash
docker build -t dockersg6/engineering-knowledge-compiler-ui:dev frontend
docker push dockersg6/engineering-knowledge-compiler-ui:dev
```

The image serves the compiled app with Nginx. In Kubernetes, the ingress sends API routes directly to the backend and all other routes to this frontend. The included Nginx proxy also makes the image usable through a direct service port-forward.

## API contract

The console uses only the API currently implemented by the Spring project:

- `GET /health` → `{ "status": "UP" }`
- `POST /api/v1/compiler/compile` with `{ "repositoryUrl": "..." }` → `{ "status": "ACCEPTED", "message": "..." }`

Run history is browser-local. It is not a backend job list and disappears when browser storage is cleared.

## Project map

```text
frontend/
├── src/App.tsx          # page layout, UI state, and interactions
├── src/api.ts           # typed calls to the Spring API
├── src/storage.ts       # browser-local compilation history
├── src/styles.css       # design system and responsive breakpoints
├── src/App.test.tsx     # API submission and validation tests
├── vite.config.ts       # local backend proxy
├── nginx.conf           # production static server and API proxy
└── Dockerfile           # Node build followed by small Nginx runtime
```

Start changes in `App.tsx` for content or behavior, `styles.css` for appearance, and `api.ts` when the backend contract changes.

## How a request travels

1. The user enters a public repository URL.
2. The UI validates the URL in the browser.
3. `api.ts` sends `POST /api/v1/compiler/compile`.
4. In development, Vite forwards the request to port `8080`.
5. In Kubernetes, ingress forwards the request to `ekc-app-service`.
6. The response is shown in the run table and saved to browser storage.

The backend currently runs the compile operation before it returns `202 Accepted`; therefore the button remains busy until the full pipeline completes. If the backend later becomes asynchronous, add a job identifier and polling endpoint to the API before showing server-side progress in the UI.

## Deploy through Argo CD

1. Build an immutable image tag from the application repository:

   ```bash
   export UI_TAG=$(git rev-parse --short HEAD)
   docker build -t dockersg6/engineering-knowledge-compiler-ui:$UI_TAG frontend
   docker push dockersg6/engineering-knowledge-compiler-ui:$UI_TAG
   ```

2. In the deployment repository, update `k8s/dev/app_config/frontend-deployment.yaml` so `image:` uses that tag.
3. Commit and push both repositories.
4. Argo CD detects the deployment-repository commit and syncs the frontend Deployment, Service, and `/` ingress route.
5. Verify the resources:

   ```bash
   kubectl get deploy,po,svc,ingress -n app-ekc-default
   kubectl rollout status deployment/ekc-frontend -n app-ekc-default
   ```

For the first experiment, the checked-in `:dev` tag works with `imagePullPolicy: Always`. Prefer immutable commit tags after the workflow is established so rollbacks are deterministic.

## Troubleshooting

- **“Compiler offline” locally:** check `curl http://localhost:8080/health`, then start the backend with `mvn spring-boot:run`.
- **Frontend loads but API calls return 404 in Kubernetes:** confirm the ingress has `/api/v1/compiler`, `/health`, and `/` paths and that `ekc-app-service` has ready endpoints.
- **Argo CD shows `ImagePullBackOff`:** confirm the frontend image/tag exists and the cluster can pull it. For a private repository, add an image pull secret.
- **A compile takes a long time:** cloning and AST parsing are synchronous today. Nginx allows 180 seconds for API responses; larger repositories may need an asynchronous backend job API.
- **History is missing on another device:** this is expected because history uses browser storage, not a backend database.
- **Fonts look slightly different without internet:** the app falls back to system fonts; the UI remains usable.
