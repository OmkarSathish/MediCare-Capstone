export type HttpMethod = "GET" | "POST" | "PUT" | "DELETE";

export interface Field {
  key: string;
  label: string;
  placeholder?: string;
  type?: "text" | "password" | "number" | "select" | "textarea" | "array";
  options?: string[];
  id?: string;
}

export interface Endpoint {
  title: string;
  desc?: string;
  method: HttpMethod;
  path: string;
  auth?: boolean;
  pathParams?: Field[];
  queryParams?: Field[];
  bodyFields?: Field[];
  onSuccess?: (data: unknown) => void;
  onOpen?: () => void;
}

export interface EndpointGroup {
  label: string;
  endpoints: { key: string; endpoint: Endpoint }[];
}
