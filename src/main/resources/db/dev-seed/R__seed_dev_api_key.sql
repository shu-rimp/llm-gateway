INSERT INTO api_keys (label, key_prefix, key_hash)
VALUES ('local-dev-seed', 'llmgw_GwTGKHYw', '4ba9fc4a235da3857ba779f33ee0ba2cc1b88b60dee20de2f18536d22c79eed2')
ON CONFLICT (key_prefix) DO NOTHING;