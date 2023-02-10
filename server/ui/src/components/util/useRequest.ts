import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import useDocusaurusContext from "@docusaurus/useDocusaurusContext";

export enum RequestStatus {
	Idle = "idle",
	Loading = "loading",
	Success = "success",
	Failure = "failure",
}

export interface RequestState {
	status: RequestStatus;
	data: null | string;
	error: null | { code: number; message: string };
}

export interface UseRequest extends RequestState {
	request: (endpoint: string, init?: RequestInit) => void;
}

const EMPTY_REQUEST: RequestState = {
	status: RequestStatus.Idle,
	data: null,
	error: null,
};

function createEndpoint(endpoint: string, apiBase: string): string {
	const segments = apiBase
		.split("/")
		.concat(endpoint.split("/"))
		.filter(Boolean);
	return `/${segments.join("/")}`;
}

function useRequest(): UseRequest {
	const [requestState, setRequest] = useState(EMPTY_REQUEST);
	const { siteConfig } = useDocusaurusContext();
	const apiBase = siteConfig.customFields?.apiBase as string;

	const isMountedRef = useRef(true);
	useEffect(() => {
		return () => {
			isMountedRef.current = false;
		};
	}, []);

	const request = useCallback(
		(endpoint: string, init?: RequestInit) => {
			setRequest((prev) => ({ ...prev, status: RequestStatus.Loading }));

			fetch(createEndpoint(endpoint, apiBase), init)
				.then((response) => {
					return response.text().then((text) => ({
						data: text,
						ok: response.ok,
						code: response.status,
					}));
				})
				.then(({ data, ok, code }) => {
					if (!isMountedRef.current) return;
					if (ok) {
						setRequest({
							status: RequestStatus.Success,
							data,
							error: null,
						});
					} else {
						setRequest((prev) => ({
							...prev,
							status: RequestStatus.Failure,
							error: { code, message: data },
						}));
					}
				})
				.catch((error) => {
					if (!isMountedRef.current) return;
					setRequest((prev) => ({
						...prev,
						status: RequestStatus.Failure,
						error: {
							code: 0,
							message: error?.toString?.() || "An unknown error occurred",
						},
					}));
				});
		},
		[apiBase],
	);

	return useMemo(() => ({ ...requestState, request }), [request, requestState]);
}

export default useRequest;
